package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.domain.model.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.feature.discovery.presentation.DiscoveryUiState
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.resource.RawText
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun DiscoveryContent(
    state: DiscoveryUiState,
    onHostSelected: (DiscoveredHost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BuildNotifyTheme.dimensions.spacing
    val layout = BuildNotifyTheme.dimensions.layout

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = layout.contentPadding),
    ) {
        Spacer(Modifier.height(spacing.xxLarge))

        Text(
            text = RawText("Discovery"),
            style = BuildNotifyTheme.typography.headingLarge,
            color = BuildNotifyTheme.colors.content.primary,
        )

        Spacer(Modifier.height(spacing.tiny))

        Text(
            text = RawText("Find Build Notify instances on your network"),
            style = BuildNotifyTheme.typography.bodyMedium,
            color = BuildNotifyTheme.colors.content.secondary,
        )

        Spacer(Modifier.height(spacing.xLarge))

        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
        ) { currentState ->
            when (currentState) {
                is DiscoveryUiState.Loading -> LoadingBody()
                is DiscoveryUiState.Content -> HostListBody(currentState.hosts, onHostSelected)
                is DiscoveryUiState.Error -> ErrorBody(currentState.message)
            }
        }
    }
}

