package me.yuriisoft.buildnotify.dispatcher

import com.intellij.openapi.diagnostic.thisLogger
import me.yuriisoft.buildnotify.build.model.BuildResult
import me.yuriisoft.buildnotify.serialization.WsMessage
import me.yuriisoft.buildnotify.server.BuildWebSocketServer
import me.yuriisoft.buildnotify.server.ClientRegistry

class BuildNotificationDispatcher(
    private val server: BuildWebSocketServer,
    private val registry: ClientRegistry
) {
    private val logger = thisLogger()

    fun onBuildStarted(projectName: String, buildId: String) {
        if (!server.isActive()) return
        server.broadcast(WsMessage.BuildStarted(projectName, buildId))
    }

    fun onBuildFinished(result: BuildResult) {
        logger.info(
            "Dispatching result: ${result.status}, " +
                    "errors=${result.errorCount}, " +
                    "warnings=${result.warningCount}, " +
                    "recipients=${registry.connectedCount}"
        )
        if (!server.isActive()) return
        server.broadcast(WsMessage.BuildResultMessage(result))
    }
}