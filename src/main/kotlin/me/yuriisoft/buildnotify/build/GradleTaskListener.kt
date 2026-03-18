package me.yuriisoft.buildnotify.build

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import me.yuriisoft.buildnotify.BuildNotifyBundle
import me.yuriisoft.buildnotify.build.model.BuildIssue
import me.yuriisoft.buildnotify.build.model.BuildStatus
import me.yuriisoft.buildnotify.dispatcher.BuildNotificationDispatcher
import me.yuriisoft.buildnotify.server.BuildWebSocketServer
import me.yuriisoft.buildnotify.server.ClientRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens to Gradle task execution via the ExternalSystem extension point.
 *
 * Three responsibilities, each handled by a dedicated method:
 *   1. [onStart]        — record start time, notify phone that build began
 *   2. [onTaskOutput]   — accumulate compiler output lines, parse errors/warnings
 *   3. [onSuccess] / [onFailure] / [onCancel] — finalize and dispatch result
 *
 * Only [ExternalSystemTaskType.EXECUTE_TASK] events are processed to ignore
 * sync and dependency resolution tasks.
 */
class GradleTaskListener : ExternalSystemTaskNotificationListener {

    private val logger = thisLogger()

    // Per-build state — both maps share the same lifecycle (start → finalize).
    private val startTimes = ConcurrentHashMap<ExternalSystemTaskId, Long>()
    private val collectedIssues = ConcurrentHashMap<ExternalSystemTaskId, MutableList<BuildIssue>>()

    private fun dispatcher() = BuildNotificationDispatcher(
        server = service<BuildWebSocketServer>(),
        registry = service<ClientRegistry>(),
    )

    override fun onStart(id: ExternalSystemTaskId, workingDir: String?) {
        if (!id.isGradleBuild()) return

        startTimes[id] = System.currentTimeMillis()
        collectedIssues[id] = mutableListOf()

        dispatcher().onBuildStarted(id.projectName(), id.toString())
        logger.info(BuildNotifyBundle.message("log.build.started", id.projectName(), id))
    }

    /**
     * Called continuously during the build with chunks of raw process output.
     *
     * [stdOut] = true  → stdout (compiler diagnostics, task results)
     * [stdOut] = false → stderr (JVM errors, Gradle internals)
     *
     * We parse both streams: Kotlin compiler writes diagnostics to stdout,
     * but some configurations route them to stderr.
     */
    override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
        if (!id.isGradleBuild()) return

        val issues = collectedIssues[id] ?: return

        // Output arrives in chunks that may contain multiple lines or partial lines.
        // Split by newline and parse each line independently.
        text.lineSequence()
            .mapNotNull { BuildOutputParser.parseLine(it) }
            .forEach { issues.add(it) }
    }

    override fun onSuccess(id: ExternalSystemTaskId) {
        if (!id.isGradleBuild()) return
        finalize(id, BuildStatus.SUCCESS)
    }

    override fun onFailure(id: ExternalSystemTaskId, e: Exception) {
        if (!id.isGradleBuild()) return
        finalize(id, BuildStatus.FAILED)
    }

    override fun onCancel(id: ExternalSystemTaskId) {
        if (!id.isGradleBuild()) return
        finalize(id, BuildStatus.CANCELLED)
    }

    private fun finalize(id: ExternalSystemTaskId, status: BuildStatus) {
        val startedAt = startTimes.remove(id) ?: System.currentTimeMillis()
        val issues = collectedIssues.remove(id).orEmpty()

        // Respect the user's setting for max issues per notification.
        val settings = service<me.yuriisoft.buildnotify.settings.PluginSettings>()
        val cappedIssues = issues.take(settings.state.maxIssuesPerNotification)

        val result = BuildResultMapper.map(
            projectName = id.projectName(),
            status = status,
            startedAt = startedAt,
            collectedIssues = cappedIssues,
        )

        dispatcher().onBuildFinished(result)
        logger.info(BuildNotifyBundle.message("log.build.finished", status, result.errorCount, result.warningCount))
    }

    private fun ExternalSystemTaskId.isGradleBuild() =
        type == ExternalSystemTaskType.EXECUTE_TASK

    private fun ExternalSystemTaskId.projectName() =
        ideProjectId.substringAfterLast("/")
}