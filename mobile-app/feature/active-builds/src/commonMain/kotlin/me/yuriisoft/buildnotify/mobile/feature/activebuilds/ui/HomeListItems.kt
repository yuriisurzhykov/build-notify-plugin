package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.morph
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_section_recent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.HeroUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.HistoryItemUi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.streamErrorItem(onIntent: (ActiveBuildsIntent) -> Unit) {
    item(key = "stream_error") {
        val spacing = GraphiteTheme.spacing
        StreamErrorBanner(
            onRetry = { onIntent(ActiveBuildsIntent.RetryStream) },
            modifier = Modifier.animateItem().padding(horizontal = spacing.heroInset).padding(bottom = spacing.m),
        )
    }
}

/** The hero morphs between states (fade + scale + size) when the build in focus changes. */
internal fun LazyListScope.heroItem(hero: HeroUi?, onIntent: (ActiveBuildsIntent) -> Unit) {
    item(key = "hero") {
        Swap(targetState = hero, transition = { morph() }, contentKey = { it?.key }, label = "hero") { current ->
            Hero(current, onIntent)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.historyItems(history: List<HistoryItemUi>, onIntent: (ActiveBuildsIntent) -> Unit) {
    if (history.isEmpty()) return
    item(key = "history_header") {
        val spacing = GraphiteTheme.spacing
        SectionHeader(
            stringResource(Res.string.builds_section_recent),
            Modifier.animateItem().padding(start = spacing.screen, end = spacing.screen, top = spacing.section),
        )
    }
    items(history, key = { it.key }) { item ->
        Box(Modifier.animateItem().padding(horizontal = GraphiteTheme.spacing.screen)) {
            val showDivider = item.key != history.last().key
            when (item) {
                is HistoryItemUi.Running  -> RunningHistoryRow(item.build, showDivider, onIntent)
                is HistoryItemUi.Finished -> FinishedHistoryRow(item.build, showDivider, onIntent)
            }
        }
    }
}

@Composable
private fun Hero(hero: HeroUi?, onIntent: (ActiveBuildsIntent) -> Unit) {
    when (hero) {
        is HeroUi.Running  -> RunningHero(hero.build, onIntent)
        is HeroUi.Finished -> when (hero.build.status) {
            FinishStatus.FAILED    -> FailedHero(hero.build, onIntent)
            FinishStatus.SUCCESS   -> SuccessHero(hero.build, onIntent)
            FinishStatus.CANCELLED -> CancelledHero(hero.build, onIntent)
        }

        null               -> EmptyHero()
    }
}
