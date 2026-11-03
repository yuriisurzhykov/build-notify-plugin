package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.templates.screens.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsUiState
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ConnectionUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.homeSections

/**
 * Home: one hero answering "is everything OK right now", then a dense history.
 *
 * Hero priority: the newest running build, else the newest finished one. Switching
 * between hero states morphs (fade + scale + size); new history rows slide in.
 */
@Composable
internal fun ActiveBuildsContent(
    state: ActiveBuildsUiState,
    connection: ConnectionUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        ScreenSurface(Modifier.fillMaxSize().safeDrawingPadding()) {
            HomeTopBar(connection)
            when (state) {
                is ActiveBuildsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BouncingDots(color = GraphiteTheme.colors.running.main)
                }

                is ActiveBuildsUiState.Empty   -> EmptyHero()
                is ActiveBuildsUiState.Loaded  -> LoadedHome(state, onIntent)
            }
        }
        val handoff = (state as? ActiveBuildsUiState.Loaded)?.handoff
        HandoffOverlay(handoff = handoff, onIntent = onIntent, onCopied = onCopied)
    }
}

@Composable
private fun LoadedHome(
    state: ActiveBuildsUiState.Loaded,
    onIntent: (ActiveBuildsIntent) -> Unit,
) {
    val sections = state.homeSections()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = GraphiteTheme.spacing.xxxl),
    ) {
        if (state.streamFailed) streamErrorItem(onIntent)
        heroItem(sections.hero, onIntent)
        historyItems(sections.history, onIntent)
    }
}
