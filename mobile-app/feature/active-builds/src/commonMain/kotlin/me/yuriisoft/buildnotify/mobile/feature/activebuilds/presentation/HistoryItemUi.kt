package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

/** One row of the home history: every build except the one shown in the hero. */
@Immutable
sealed interface HistoryItemUi {

    val key: String

    @Immutable
    data class Running(val build: RunningBuildUi) : HistoryItemUi {
        override val key: String = "h_running_${build.buildId}"
    }

    @Immutable
    data class Finished(val build: FinishedBuildUi) : HistoryItemUi {
        override val key: String = "h_finished_${build.buildId}"
    }
}
