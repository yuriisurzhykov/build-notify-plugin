package me.yuriisoft.buildnotify.mobile.feature.discovery.presentation

sealed interface DiscoveryEvent {

    data class NavigateToBuild(val host: String, val port: Int) : DiscoveryEvent
}
