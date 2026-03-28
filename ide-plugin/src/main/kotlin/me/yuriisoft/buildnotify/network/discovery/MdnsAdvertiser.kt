package me.yuriisoft.buildnotify.network.discovery

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.security.CertificateManager
import me.yuriisoft.buildnotify.settings.PluginSettingsState
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

/**
 * Advertises the BuildNotify WebSocket server on the local network via mDNS.
 *
 * ### Phase 3 change — `id` TXT key
 * The TXT record now includes `id = <instanceId>` in addition to `scheme`,
 * `version`, and `fp`. This allows mobile clients to key their TOFU trust
 * store on a **stable server identity** rather than on the mDNS service name,
 * which is user-configurable and can change.
 *
 * [InstanceIdentity] is the single source of truth for the instance UUID.
 * Both this class and [BuildWebSocketServer] resolve it from the service
 * container — no circular dependency, no constructor-level coupling.
 *
 * Mobile clients running older plugin versions that don't yet advertise `id`
 * fall back gracefully to `host.name` as the trust key (see [DiscoveryViewModel]).
 */
@Service(Service.Level.APP)
class MdnsAdvertiser : Disposable {

    companion object {
        const val SERVICE_TYPE = "_buildnotify._tcp.local."
    }

    private val logger = thisLogger()
    private val started = AtomicBoolean(false)
    private val jmDNS: AtomicReference<JmDNS?> = AtomicReference(null)

    fun start() {
        if (!started.compareAndSet(false, true)) return

        runCatching {
            val settings = service<PluginSettingsState>().snapshot()
            val certManager = CertificateManager.getInstance()
            val instanceId = service<InstanceIdentity>().id

            val txtRecord = buildMap {
                put("version", "1")
                put("id", instanceId)
                val fingerprintHash = certManager.fingerprint()
                if (fingerprintHash != null) {
                    put("scheme", "wss")
                    put("fp", fingerprintHash)
                } else {
                    put("scheme", "ws")
                }
            }

            val mDnsInstance = JmDNS.create(InetAddress.getLocalHost())
            val info = ServiceInfo.create(
                SERVICE_TYPE,
                settings.serviceName,
                settings.port,
                0,
                0,
                txtRecord,
            )

            mDnsInstance.registerService(info)
            jmDNS.getAndSet(mDnsInstance)
            logger.info("mDNS advertiser started: ${settings.serviceName}:${settings.port}, instanceId=$instanceId")
        }.onFailure { throwable ->
            started.set(false)
            logger.error("Failed to start mDNS advertiser", throwable)
        }
    }

    fun stop() {
        runCatching { jmDNS.getAndSet(null)?.close() }
            .onFailure { throwable -> logger.warn("Failed to stop mDNS advertiser cleanly", throwable) }
        started.set(false)
    }

    override fun dispose() {
        stop()
    }
}