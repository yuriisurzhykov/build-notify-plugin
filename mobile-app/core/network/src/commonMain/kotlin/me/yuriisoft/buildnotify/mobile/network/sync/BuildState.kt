package me.yuriisoft.buildnotify.mobile.network.sync

import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus

/**
 * Domain-level representation of a single build's lifecycle.
 *
 * [Active] describes a build that is currently executing. [Completed] wraps the final [BuildResult] once
 * the server sends `build.result`.
 *
 * Used as the value type in [BuildStateSync.builds], keyed by [buildId].
 */
sealed interface BuildState {

    val buildId: String
    val projectName: String

    data class Active(
        override val buildId: String,
        override val projectName: String,
        val startedAt: Long,
        /** The most recently started task; a build runs thousands, only this one is kept. */
        val currentTask: String? = null,
    ) : BuildState

    data class Completed(val result: BuildResult) : BuildState {
        override val buildId: String get() = result.buildId
        override val projectName: String get() = result.projectName
    }
}
