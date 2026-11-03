package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable

/** What the home hero shows: the newest running build, else the newest finished one. */
@Immutable
sealed interface HeroUi {

    /** Stable key; different for running and finished so the hero morphs between them. */
    val key: String

    @Immutable
    data class Running(val build: RunningBuildUi) : HeroUi {
        override val key: String = "running_${build.buildId}"
    }

    @Immutable
    data class Finished(val build: FinishedBuildUi) : HeroUi {
        override val key: String = "finished_${build.buildId}"
    }
}
