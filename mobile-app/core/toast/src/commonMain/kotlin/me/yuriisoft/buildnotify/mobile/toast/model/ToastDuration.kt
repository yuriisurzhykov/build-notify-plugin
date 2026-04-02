package me.yuriisoft.buildnotify.mobile.toast.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface ToastDuration {
    data object Indefinite : ToastDuration
    data class Timed(val duration: Duration = 3.seconds) : ToastDuration
}
