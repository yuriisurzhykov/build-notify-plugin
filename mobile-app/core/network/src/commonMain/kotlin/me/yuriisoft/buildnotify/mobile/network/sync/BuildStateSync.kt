package me.yuriisoft.buildnotify.mobile.network.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildSnapshotPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskFinishedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession
import kotlin.time.Clock

/**
 * Maintains a live, consistent map of all known builds by folding
 * every incoming build-related payload into an immutable state snapshot.
 *
 * ### Pipeline
 *
 * ```
 * session.incoming                           // raw WsPayload stream
 *     .runningFold(emptyMap()) { … }         // accumulate into Map<buildId, BuildState>
 *     .stateIn(scope, Eagerly, emptyMap())   // expose as StateFlow
 * ```
 *
 * [BuildSnapshotPayload] replaces the entire map (server-authoritative sync
 * point on connect). All other `build.*` events are merged incrementally.
 *
 * **Immutability:** every fold iteration produces a new [Map]; no mutable
 * fields, no `_state` backing properties.
 *
 * **Boundary `when`:** the single `when` maps network protocol types to
 * domain state — legitimate at the system boundary per project coding rules.
 */
class BuildStateSync(
    session: ActiveSession,
    dispatchers: AppDispatchers,
) {
    private val scope = CoroutineScope(dispatchers.io + SupervisorJob())

    val builds: StateFlow<Map<String, BuildState>> = session.incoming
        .runningFold(emptyMap<String, BuildState>()) { builds, payload ->
            when (payload) {
                is BuildSnapshotPayload -> payload.toStateMap()
                is BuildStartedPayload  -> builds.withActiveBuild(payload)
                is TaskStartedPayload   -> builds.withTask(
                    payload.buildId,
                    payload.taskPath,
                    TaskStatus.RUNNING
                )

                is TaskFinishedPayload  -> builds.withTask(
                    payload.buildId,
                    payload.taskPath,
                    payload.status
                )

                is BuildResultPayload   -> builds.withCompleted(payload.result)
                else                    -> builds
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())
}

private fun BuildSnapshotPayload.toStateMap(): Map<String, BuildState> =
    activeBuilds.associate { info ->
        info.buildId to BuildState.Active(
            buildId = info.buildId,
            projectName = info.projectName,
            startedAt = info.startedAt,
            tasks = info.tasks.associate { it.taskPath to it.status },
        )
    }

private fun Map<String, BuildState>.withActiveBuild(
    payload: BuildStartedPayload,
): Map<String, BuildState> =
    this + (payload.buildId to BuildState.Active(
        buildId = payload.buildId,
        projectName = payload.projectName,
        startedAt = Clock.System.now().toEpochMilliseconds(),
    ))

private fun Map<String, BuildState>.withTask(
    buildId: String,
    taskPath: String,
    status: TaskStatus,
): Map<String, BuildState> {
    val active = this[buildId] as? BuildState.Active ?: return this
    return this + (buildId to active.copy(tasks = active.tasks + (taskPath to status)))
}

private fun Map<String, BuildState>.withCompleted(
    result: BuildResult,
): Map<String, BuildState> =
    this + (result.buildId to BuildState.Completed(result))
