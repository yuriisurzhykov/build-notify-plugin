package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus

/**
 * Maps a SQL row pair ([BuildRecord] + its [BuildIssueRecord] list) to a domain [BuildSnapshot].
 *
 * For [BuildRecordStatus.ACTIVE], issues are ignored and `currentTask` is preserved.
 * For terminal statuses, issues are split by [IssueSeverity] and [BuildOutcome] is reconstructed
 * from the normalized `duration_ms` column and the `build_issue` rows.
 */
class BuildSnapshotMapper(
    private val issueMapper: IssueRecordMapper = IssueRecordMapper(),
) : Mapper<Pair<BuildRecord, List<BuildIssueRecord>>, BuildSnapshot> {

    override fun map(from: Pair<BuildRecord, List<BuildIssueRecord>>): BuildSnapshot {
        val (record, issues) = from
        return when (record.status) {
            BuildRecordStatus.ACTIVE    -> BuildSnapshot.Active(
                buildId = record.buildId,
                projectName = record.projectName,
                startedAt = record.startedAt,
                currentTask = record.currentTask,
            )

            BuildRecordStatus.SUCCESS,
            BuildRecordStatus.FAILED,
            BuildRecordStatus.CANCELLED -> BuildSnapshot.Finished(
                buildId = record.buildId,
                projectName = record.projectName,
                startedAt = record.startedAt,
                outcome = BuildOutcome(
                    status = record.status.toFinishStatus(),
                    durationMs = checkNotNull(record.durationMs) {
                        "Finished build '${record.buildId}' has null durationMs. " +
                            "This is a data-integrity violation in the local cache."
                    },
                    errors = issues
                        .filter { it.severity == IssueSeverity.ERROR }
                        .map(issueMapper::map),
                    warnings = issues
                        .filter { it.severity == IssueSeverity.WARNING }
                        .map(issueMapper::map),
                ),
            )
        }
    }
}

private fun BuildRecordStatus.toFinishStatus(): FinishStatus = when (this) {
    BuildRecordStatus.ACTIVE    -> error(
        "ACTIVE status cannot map to FinishStatus — call site should guard with status check.",
    )
    BuildRecordStatus.SUCCESS   -> FinishStatus.SUCCESS
    BuildRecordStatus.FAILED    -> FinishStatus.FAILED
    BuildRecordStatus.CANCELLED -> FinishStatus.CANCELLED
}
