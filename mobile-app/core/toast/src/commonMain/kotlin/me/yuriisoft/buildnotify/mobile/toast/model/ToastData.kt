package me.yuriisoft.buildnotify.mobile.toast.model

import me.yuriisoft.buildnotify.mobile.ui.resource.ImageResource
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ToastData @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val type: ToastType,
    val message: TextResource,
    val icon: ImageResource? = null,
    val duration: ToastDuration = type.defaultDuration,
)
