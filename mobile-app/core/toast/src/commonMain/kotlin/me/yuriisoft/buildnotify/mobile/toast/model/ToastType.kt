package me.yuriisoft.buildnotify.mobile.toast.model

enum class ToastType(
    val priority: Int,
    val isSticky: Boolean,
    val defaultDuration: ToastDuration,
) {
    Info(
        priority = 0,
        isSticky = false,
        defaultDuration = ToastDuration.Timed(),
    ),
    Success(
        priority = 1,
        isSticky = false,
        defaultDuration = ToastDuration.Timed(),
    ),
    Warning(
        priority = 2,
        isSticky = false,
        defaultDuration = ToastDuration.Indefinite,
    ),
    Error(
        priority = 3,
        isSticky = true,
        defaultDuration = ToastDuration.Indefinite,
    ),
}
