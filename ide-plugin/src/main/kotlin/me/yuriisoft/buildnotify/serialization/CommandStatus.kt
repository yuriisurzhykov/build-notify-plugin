package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.Serializable

@Serializable
enum class CommandStatus {
    /** Command queued or already in progress; result will follow as a build event. */
    ACCEPTED,

    /** Command understood but refused (unknown buildId, wrong state, insufficient capability). */
    REJECTED,

    /** Server-side error during execution. */
    FAILED,
}
