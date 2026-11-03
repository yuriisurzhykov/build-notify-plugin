package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

/**
 * Turns a failed build into a message a coding agent (Claude Code, Codex, …) can act on.
 *
 * Written in English on purpose: it is read by an agent next to English code and
 * compiler output, whatever the phone's language is.
 */
object HandoffPrompt {

    /** Upper bound on attached log lines; keeps the message shareable. */
    const val MAX_LOG_LINES: Int = 200

    /**
     * @param logs the tail of the log to attach, or `null` to attach none
     * @param totalLines how long the whole log is; only the tail is ever loaded
     */
    fun build(
        build: BuildSnapshot.Finished,
        logs: List<BuildLogEntry>?,
        totalLines: Int = logs?.size ?: 0,
    ): String = buildString {
        val outcome = build.outcome
        append("The Gradle build of `${build.projectName}` failed after ${formatDuration(outcome.durationMs)}.\n")
        if (outcome.errors.isEmpty()) {
            append("It reported no compiler errors; the cause is in the build log.\n")
        } else {
            append("Fix these ${outcome.errors.size} error(s), then rebuild:\n")
            outcome.errors.forEach { issue ->
                append('\n')
                val location = issue.toUi().location
                if (location != null) append(location).append('\n')
                append("  ").append(issue.message).append('\n')
            }
        }
        if (outcome.warnings.isNotEmpty()) {
            append(
                "\n${outcome.warnings.size} warning(s) were also reported; leave them unless they cause the failure.\n",
            )
        }
        if (!logs.isNullOrEmpty()) {
            val tail = logs.takeLast(MAX_LOG_LINES)
            append("\nBuild log")
            val total = maxOf(totalLines, logs.size)
            if (tail.size < total) append(" (last ${tail.size} of $total lines)")
            append(":\n```\n")
            tail.forEach { append(it.message).append('\n') }
            append("```\n")
        }
    }
}
