package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class RunningBuildUi(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
    val currentTask: String?,
    val recentLogs: ImmutableList<LogLineUi>,
)
