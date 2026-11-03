package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus

@Immutable
data class FinishedBuildUi(
    val buildId: String,
    val projectName: String,
    val status: FinishStatus,
    val startedAt: Long,
    val durationMs: Long,
    val errorCount: Int,
    val warningCount: Int,
    val topErrors: ImmutableList<BuildIssueUi>,
)
