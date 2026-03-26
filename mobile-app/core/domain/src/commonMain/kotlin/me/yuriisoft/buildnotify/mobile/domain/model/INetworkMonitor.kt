package me.yuriisoft.buildnotify.mobile.domain.model

import kotlinx.coroutines.flow.StateFlow

/**
 * Observes local-network availability (WiFi / Ethernet).
 *
 * mDNS discovery only works over a local network, so the discovery screen
 * checks this before starting an NSD scan. Platform implementations live in
 * `:core:data` androidMain / iosMain.
 */
interface INetworkMonitor {
    val isNetworkAvailable: StateFlow<Boolean>
}
