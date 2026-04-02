package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import me.yuriisoft.buildnotify.mobile.toast.model.ToastData

/**
 * An atomic side-effect triggered by a [ConnectionState][me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState]
 * transition.
 *
 * Each subtype carries its own polymorphic [execute], so the pipeline
 * never needs a `when` to dispatch behavior — the type decides.
 */
sealed interface ToastAction {

    fun execute(executor: ToastActionExecutor)

    data class Show(val data: ToastData) : ToastAction {
        override fun execute(executor: ToastActionExecutor) = executor.show(data)
    }

    data class Dismiss(val id: String) : ToastAction {
        override fun execute(executor: ToastActionExecutor) = executor.dismiss(id)
    }

    data object NavigateToDiscovery : ToastAction {
        override fun execute(executor: ToastActionExecutor) = executor.navigateToDiscovery()
    }
}
