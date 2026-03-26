package me.yuriisoft.buildnotify.mobile.data.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.yuriisoft.buildnotify.mobile.domain.model.INetworkMonitor

/**
 * iOS implementation of [INetworkMonitor] using NWPathMonitor via cinterop.
 *
 * NWPathMonitor wiring is deferred to Phase 4.
 * Returns `true` by default so that discovery is not blocked on iOS stubs.
 */
class IosNetworkMonitor : INetworkMonitor {

    override val isNetworkAvailable: StateFlow<Boolean> =
        MutableStateFlow(true)
}
