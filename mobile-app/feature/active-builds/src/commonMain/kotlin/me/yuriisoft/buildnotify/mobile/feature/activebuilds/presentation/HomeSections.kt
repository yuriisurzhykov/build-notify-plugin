package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** The home screen split into its two parts. */
@Immutable
data class HomeSections(
    val hero: HeroUi?,
    val history: ImmutableList<HistoryItemUi>,
)

/**
 * Hero priority: the newest running build, else the newest finished one. Everything else
 * goes to the history, running builds first. Pure, so the choice is unit-tested.
 */
fun ActiveBuildsUiState.Loaded.homeSections(): HomeSections {
    val hero = running.firstOrNull()?.let { HeroUi.Running(it) }
        ?: finished.firstOrNull()?.let { HeroUi.Finished(it) }
    val history = running
        .filterNot { hero is HeroUi.Running && hero.build.buildId == it.buildId }
        .map { HistoryItemUi.Running(it) } +
        finished
            .filterNot { hero is HeroUi.Finished && hero.build.buildId == it.buildId }
            .map { HistoryItemUi.Finished(it) }
    return HomeSections(hero = hero, history = history.toImmutableList())
}
