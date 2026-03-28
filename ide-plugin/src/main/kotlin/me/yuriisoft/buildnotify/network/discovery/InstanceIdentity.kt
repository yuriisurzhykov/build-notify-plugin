package me.yuriisoft.buildnotify.network.discovery

import com.intellij.openapi.components.Service
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Stable identifier for this IDE process instance.
 *
 * The [id] is generated once per JVM lifetime and never changes while the IDE
 * is running. It resets on restart — clients treat a new [id] as a new server
 * and re-establish TOFU trust automatically.
 *
 * Extracted from [BuildWebSocketServer] so that both the WebSocket server and
 * [MdnsAdvertiser] can reference it without creating a circular service
 * dependency (DIP — both depend on this abstraction, not on each other).
 *
 * Registered as `@Service(Level.APP)` — one instance for the entire IDE
 * process; no per-project lifecycle concerns (SRP).
 */
@Service(Service.Level.APP)
class InstanceIdentity {

    /**
     * A random UUID, stable for the lifetime of this IDE process.
     *
     * `@OptIn` is scoped to the property so the experimental annotation
     * does not leak to callers.
     */
    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.random().toString()
}