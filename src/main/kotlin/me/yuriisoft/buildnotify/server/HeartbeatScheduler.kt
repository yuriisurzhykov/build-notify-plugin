package me.yuriisoft.buildnotify.server

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.*
import me.yuriisoft.buildnotify.serialization.MessageSerializer
import me.yuriisoft.buildnotify.serialization.WsMessage
import me.yuriisoft.buildnotify.settings.PluginSettings

/**
 * SRP: only sends periodic heartbeats.
 *
 * Constructor dependencies — DIP is met.
 * Doesn't know where the registry and settings come from.
 *
 * SupervisorJob: an exception in a single tick doesn't kill the entire scheduler.
 * Lifecycle: strictly start() → stop(), not tied to the IDE application scope.
 */
class HeartbeatScheduler(
    private val registry: ClientRegistry,
    private val settings: PluginSettings,
) {
    private val logger = thisLogger()

    private val scope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.IO +
                CoroutineName("HeartbeatScheduler") +
                CoroutineExceptionHandler { _, throwable ->
                    logger.warn("HeartbeatScheduler: unhandled exception", throwable)
                }
    )

    private var tickJob: Job? = null

    fun start() {
        if (tickJob?.isActive == true) return

        val intervalMs = settings.state.heartbeatIntervalSec * 1_000L

        tickJob = scope.launch {
            logger.info("Starting heartbeat scheduler... Interval ${intervalMs}ms")
            delay(intervalMs)
            while (isActive) {
                tick()
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        tickJob?.cancel()
        tickJob = null
        scope.cancel()
        logger.info("Stopping heartbeat scheduler")
    }

    private fun tick() {
        if (registry.isEmpty()) {
            logger.debug("Heartbeat skipped: no connected clients.")
        }

        runCatching {
            val message = MessageSerializer.encode(WsMessage.Heartbeat())
            registry.broadcast(message)
            logger.debug("Heartbeat sent to ${registry.connectedCount} client(s).")
        }.onFailure {
            logger.warn("Heartbeat scheduler failed to send: ${it.message}", it)
        }
    }
}