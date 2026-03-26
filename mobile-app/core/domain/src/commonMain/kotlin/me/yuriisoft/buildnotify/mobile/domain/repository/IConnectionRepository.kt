package me.yuriisoft.buildnotify.mobile.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.yuriisoft.buildnotify.mobile.domain.model.BuildResult
import me.yuriisoft.buildnotify.mobile.domain.model.ConnectionStatus
import me.yuriisoft.buildnotify.mobile.domain.model.DiscoveredHost

/**
 * Application-scoped contract for managing the WebSocket connection
 * to a Build Notify IDE plugin instance.
 *
 * Follows DIP: the domain and feature layers depend only on this abstraction;
 * the concrete implementation ([ConnectionManager] backed by Ktor) lives in
 * `:core:data`.
 *
 * [status] is the single source of truth for the connection lifecycle.
 * [buildEvents] emits build results only while the connection is [ConnectionStatus.Connected].
 */
interface IConnectionRepository {
    val status: StateFlow<ConnectionStatus>
    val buildEvents: Flow<BuildResult>
    suspend fun connect(host: DiscoveredHost)
    suspend fun disconnect()
}
