package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import me.yuriisoft.buildnotify.mobile.toast.model.ToastData

/**
 * Thin contract that [ToastAction] subtypes dispatch to, keeping each
 * action decoupled from the concrete toast/navigation infrastructure.
 *
 * ISP: every [ToastAction] subtype calls exactly the method it needs;
 * no subtype is forced to depend on capabilities it does not use.
 */
interface ToastActionExecutor {
    fun show(data: ToastData)
    fun dismiss(id: String)
    fun navigateToDiscovery()
}
