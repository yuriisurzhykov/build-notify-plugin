package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.discovery_subtitle
import build_notify_mobile.feature.discovery.generated.resources.discovery_title
import build_notify_mobile.feature.discovery.generated.resources.discovery_version_footer
import me.yuriisoft.buildnotify.mobile.designsystem.components.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.motion.GraphiteMotion
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.discovery.presentation.DiscoveryUiState
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import org.jetbrains.compose.resources.stringResource

/**
 * Welcome / discovery flow.
 *
 * The body stage keeps the original directional transitions (forward → slide left,
 * back → slide right, same step or network loss → cross-fade). Around it, the stage
 * backdrop and the brand glyph re-tint smoothly to the state's [BuildTone].
 */
@Composable
internal fun DiscoveryContent(
    state: DiscoveryUiState,
    appVersion: String,
    onHostSelected: (DiscoveredHost) -> Unit,
    onConfirmPairing: () -> Unit,
    onRejectPairing: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GraphiteTheme.colors
    val spacing = GraphiteTheme.spacing
    val tone = state.tone()
    val backdrop by animateColorAsState(colors.tone(tone).hero, tween(GraphiteMotion.Slow), label = "backdrop")
    val accent by animateColorAsState(colors.tone(tone).main, tween(GraphiteMotion.Slow), label = "accent")
    val accentContainer by animateColorAsState(colors.tone(tone).container, tween(GraphiteMotion.Slow), label = "accentContainer")

    ScreenSurface(modifier.fillMaxSize().safeDrawingPadding()) {
        Reveal(index = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screen, vertical = spacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(spacing.l),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(accentContainer, GraphiteTheme.shapes.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(HubIcon, contentDescription = null, tint = accent, size = 26.dp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    Text(stringResource(Res.string.discovery_title), style = GraphiteTheme.typography.display)
                    Text(
                        stringResource(Res.string.discovery_subtitle),
                        style = GraphiteTheme.typography.bodySmall,
                        color = colors.contentSecondary,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = spacing.heroInset)
                .clip(GraphiteTheme.shapes.hero)
                .background(backdrop),
        ) {
            AnimatedContent(
                targetState = state,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val isForward = targetState.animateOrder > initialState.animateOrder
                    val isBackward = targetState.animateOrder < initialState.animateOrder
                    val isFade = targetState.animateOrder == initialState.animateOrder ||
                        targetState.animateOrder < 0
                    // Same branch order as before the redesign: transitions are unchanged.
                    when {
                        isForward  -> slideInHorizontally { fullWidth -> fullWidth } togetherWith slideOutHorizontally { fullWidth -> -fullWidth }
                        isBackward -> slideInHorizontally { fullWidth -> -fullWidth } togetherWith slideOutHorizontally { fullWidth -> fullWidth }
                        isFade     -> fadeIn() togetherWith fadeOut()
                        else       -> fadeIn() togetherWith fadeOut()
                    }
                },
                contentKey = { it::class.qualifiedName },
                label = "discoveryStage",
            ) { currentState ->
                when (currentState) {
                    is DiscoveryUiState.Idle                -> IdleBody(onStartScan = onStartScan)
                    is DiscoveryUiState.Scanning            -> ScanningBody(onCancel = onCancel)
                    is DiscoveryUiState.ServiceSelection    -> HostListBody(currentState.hosts, onHostSelected)
                    is DiscoveryUiState.PairingConfirmation -> PairingConfirmationBody(
                        host = currentState.host,
                        pin = currentState.pin,
                        onConfirm = onConfirmPairing,
                        onReject = onRejectPairing,
                    )

                    is DiscoveryUiState.WaitingForIde       -> WaitingForIdeBody(currentState.host, onCancel)
                    is DiscoveryUiState.Connecting          -> ConnectingBody(currentState.host, onCancel)
                    is DiscoveryUiState.Connected           -> ConnectedBody(currentState.host)
                    is DiscoveryUiState.ConnectionFailed    -> ConnectionFailedBody(
                        host = currentState.hostResource,
                        reason = currentState.reasonResource,
                        onRetry = { onHostSelected(currentState.host) },
                    )

                    is DiscoveryUiState.NothingFound        -> EmptyBody(onRetry = onRetry)
                    is DiscoveryUiState.ScanError           -> ErrorBody(currentState.message, onRetry)
                    is DiscoveryUiState.NetworkUnavailable  -> NetworkUnavailableBody()
                }
            }
        }

        Text(
            text = stringResource(Res.string.discovery_version_footer, appVersion),
            style = GraphiteTheme.typography.caption,
            color = colors.contentTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.m),
        )
    }
}

/** The colour story of the flow: blue while working, amber when the user must act. */
internal fun DiscoveryUiState.tone(): BuildTone = when (this) {
    DiscoveryUiState.Idle,
    DiscoveryUiState.Scanning,
    is DiscoveryUiState.ServiceSelection,
    is DiscoveryUiState.Connecting          -> BuildTone.Running

    is DiscoveryUiState.PairingConfirmation,
    is DiscoveryUiState.WaitingForIde,
    DiscoveryUiState.NothingFound,
    DiscoveryUiState.NetworkUnavailable     -> BuildTone.Warning

    is DiscoveryUiState.Connected           -> BuildTone.Success

    is DiscoveryUiState.ConnectionFailed,
    is DiscoveryUiState.ScanError           -> BuildTone.Failure
}
