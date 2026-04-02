package me.yuriisoft.buildnotify.mobile.toast

internal data class ToastTransition(
    val snapshot: ToastSnapshot,
    val effects: List<ToastSideEffect> = emptyList(),
)