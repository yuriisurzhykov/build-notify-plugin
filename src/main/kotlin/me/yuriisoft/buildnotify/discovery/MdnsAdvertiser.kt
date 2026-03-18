package me.yuriisoft.buildnotify.discovery

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.settings.PluginSettings
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

@Service(Service.Level.APP)
class MdnsAdvertiser : AutoCloseable {

    companion object {
        const val SERVICE_TYPE = "_buildnotify._tcp.local."
    }

    private val logger = thisLogger()
    private var jmDNS: JmDNS? = null

    fun start() {
        runCatching {
            val settings = service<PluginSettings>()

            val instance = JmDNS.create(InetAddress.getLocalHost())
            val info = ServiceInfo.create(
                SERVICE_TYPE,
                settings.state.serviceName,
                settings.state.port,
                /* weight   = */ 0,
                /* priority = */ 0,
                mapOf("version" to "1"),
            )

            instance.registerService(info)
            jmDNS = instance
        }.onFailure { e ->
            logger.error("Failed to start mDNS advertiser", e)
        }
    }

    override fun close() {
        runCatching { jmDNS?.close() }
            .onFailure { e -> logger.error("Failed to close mDNS advertiser", e) }
        jmDNS = null
        logger.info("mDNS advertiser closed")
    }
}