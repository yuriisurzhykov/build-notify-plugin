package me.yuriisoft.buildnotify.mobile.toast

import kotlinx.coroutines.CompletableDeferred
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastResult

internal class ToastTicket(val data: ToastData) {

    val id: String get() = data.id

    private val deferred = CompletableDeferred<ToastResult>()

    fun complete(result: ToastResult) {
        deferred.complete(result)
    }

    fun cancel() {
        deferred.cancel()
    }

    suspend fun awaitResult(): ToastResult = deferred.await()

    /**
     * Boundary mapping: domain duration → internal timer effect.
     * */
    fun timerEffects(): List<ToastSideEffect> = when (val d = data.duration) {
        is ToastDuration.Indefinite -> emptyList()
        is ToastDuration.Timed      -> listOf(ToastSideEffect.ScheduleTimer(id, d.duration))
    }
}