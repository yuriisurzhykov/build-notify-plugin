package me.yuriisoft.buildnotify.serialization

import kotlinx.serialization.Serializable

@Serializable
enum class Capability {
    BUILD_MONITOR,   // can receive build events
    BUILD_CONTROL,   // can send cmd.run_build / cmd.cancel_build
    AI_AGENT,        // can send cmd.trigger_agent (future)
}
