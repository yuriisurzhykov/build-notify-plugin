package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.Serializable

@Serializable
enum class Capability {
    BUILD_MONITOR,
    BUILD_CONTROL,
    AI_AGENT,
}
