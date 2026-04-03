package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class FinishStatus {
    SUCCESS,
    FAILED,
    CANCELLED,
}
