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
 * point on connect). Started, task-started and result events are merged in; per-task
 * history is not kept here (the build cache owns the log), so each event is O(1) in
 * the number of tasks. Unchanged events return the same map, and the [StateFlow]
 * does not emit for them.
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
                is TaskStartedPayload   -> builds.withCurrentTask(payload.buildId, payload.taskPath)
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
            currentTask = info.tasks.lastOrNull { it.status == TaskStatus.RUNNING }?.taskPath,
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

private fun Map<String, BuildState>.withCurrentTask(
    buildId: String,
    taskPath: String,
): Map<String, BuildState> {
    val active = this[buildId] as? BuildState.Active ?: return this
    if (active.currentTask == taskPath) return this
    return this + (buildId to active.copy(currentTask = taskPath))
}

/** Completed builds are kept only up to [MAX_COMPLETED]; the map must not grow forever. */
private fun Map<String, BuildState>.withCompleted(
    result: BuildResult,
): Map<String, BuildState> {
    val updated = this + (result.buildId to BuildState.Completed(result))
    val completed = updated.values.filterIsInstance<BuildState.Completed>()
    if (completed.size <= MAX_COMPLETED) return updated
    val evicted = completed
        .sortedBy { it.result.finishedAt }
        .take(completed.size - MAX_COMPLETED)
        .mapTo(HashSet()) { it.buildId }
    return updated - evicted
}

private const val MAX_COMPLETED = 10
