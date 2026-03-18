package me.yuriisoft.buildnotify.build

import me.yuriisoft.buildnotify.build.model.BuildIssue
import me.yuriisoft.buildnotify.build.model.BuildResult
import me.yuriisoft.buildnotify.build.model.BuildStatus
import java.util.*

/**
 * Stateless mapper. SRP: assembles a [BuildResult] from already-parsed data.
 *
 * [collectedIssues] arrive pre-parsed by [BuildOutputParser] — no further
 * processing of raw text happens here.
 */
object BuildResultMapper {

    fun map(
        projectName: String,
        status: BuildStatus,
        startedAt: Long,
        collectedIssues: List<BuildIssue>,
    ): BuildResult {
        val finishedAt = System.currentTimeMillis()

        return BuildResult(
            id = UUID.randomUUID().toString(),
            projectName = projectName,
            status = status,
            durationMs = finishedAt - startedAt,
            errors = collectedIssues.filter { it.severity == BuildIssue.Severity.ERROR },
            warnings = collectedIssues.filter { it.severity == BuildIssue.Severity.WARNING },
            startedAt = startedAt,
            finishedAt = finishedAt,
        )
    }
}