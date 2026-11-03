package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.discovery_subtitle
import build_notify_mobile.feature.discovery.generated.resources.discovery_title
import build_notify_mobile.feature.discovery.generated.resources.discovery_version_footer
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.IconBadge
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.IconBadgeSize
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.GraphiteTransition
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.templates.screens.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.templates.screens.TintedStage
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
    val tone = state.tone()

    ScreenSurface(modifier.fillMaxSize().safeDrawingPadding()) {
        DiscoveryHeader(tone)

        TintedStage(tone = tone, modifier = Modifier.weight(1f).fillMaxWidth()) {
            Swap(
                targetState = state,
                modifier = Modifier.fillMaxSize(),
                // Unchanged since before the redesign: forward slides left, back slides
                // right, the same step cross-fades (see GraphiteTransition.Directional).
                transition = GraphiteTransition.Directional<DiscoveryUiState> { it.animateOrder },
                contentKey = { it::class.qualifiedName },
                label = "discoveryStage",
            ) { currentState ->
                DiscoveryStage(
                    state = currentState,
                    onHostSelected = onHostSelected,
                    onConfirmPairing = onConfirmPairing,
                    onRejectPairing = onRejectPairing,
                    onRetry = onRetry,
                    onCancel = onCancel,
                    onStartScan = onStartScan,
                )
            }
        }

        VersionFooter(appVersion)
    }
}

/** Brand glyph and title; the glyph re-tints with the state's tone. */
@Composable
private fun DiscoveryHeader(tone: BuildTone) {
    val spacing = GraphiteTheme.spacing
    Reveal(index = 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(spacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(HubIcon, tone, size = IconBadgeSize.Small)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(stringResource(Res.string.discovery_title), style = GraphiteTheme.typography.display)
                Text(
                    stringResource(Res.string.discovery_subtitle),
                    style = GraphiteTheme.typography.bodySmall,
                    color = GraphiteTheme.colors.contentSecondary,
                )
            }
        }
    }
}

@Composable
private fun VersionFooter(appVersion: String) {
    Text(
        text = stringResource(Res.string.discovery_version_footer, appVersion),
        style = GraphiteTheme.typography.caption,
        color = GraphiteTheme.colors.contentTertiary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GraphiteTheme.spacing.m),
    )
}

/** Renders one discovery step. */
@Composable
private fun DiscoveryStage(
    state: DiscoveryUiState,
    onHostSelected: (DiscoveredHost) -> Unit,
    onConfirmPairing: () -> Unit,
    onRejectPairing: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onStartScan: () -> Unit,
) {
    when (state) {
        is DiscoveryUiState.Idle                -> IdleBody(onStartScan = onStartScan)
        is DiscoveryUiState.Scanning            -> ScanningBody(onCancel = onCancel)
        is DiscoveryUiState.ServiceSelection    -> HostListBody(state.hosts, onHostSelected)
        is DiscoveryUiState.PairingConfirmation -> PairingConfirmationBody(
            host = state.host,
            pin = state.pin,
            onConfirm = onConfirmPairing,
            onReject = onRejectPairing,
        )

        is DiscoveryUiState.WaitingForIde       -> WaitingForIdeBody(state.host, onCancel)
        is DiscoveryUiState.Connecting          -> ConnectingBody(state.host, onCancel)
        is DiscoveryUiState.Connected           -> ConnectedBody(state.host)
        is DiscoveryUiState.ConnectionFailed    -> ConnectionFailedBody(
            host = state.hostResource,
            reason = state.reasonResource,
            onRetry = { onHostSelected(state.host) },
        )

        is DiscoveryUiState.NothingFound        -> EmptyBody(onRetry = onRetry)
        is DiscoveryUiState.ScanError           -> ErrorBody(state.message, onRetry)
        is DiscoveryUiState.NetworkUnavailable  -> NetworkUnavailableBody()
    }
}

/** The colour story of the flow: blue while working, amber when the user must act. */
internal fun DiscoveryUiState.tone(): BuildTone = when (this) {
    is DiscoveryUiState.Idle,
    is DiscoveryUiState.Scanning,
    is DiscoveryUiState.ServiceSelection,
    is DiscoveryUiState.Connecting          -> BuildTone.Running

    is DiscoveryUiState.PairingConfirmation,
    is DiscoveryUiState.WaitingForIde,
    is DiscoveryUiState.NothingFound,
    is DiscoveryUiState.NetworkUnavailable  -> BuildTone.Warning

    is DiscoveryUiState.Connected           -> BuildTone.Success

    is DiscoveryUiState.ConnectionFailed,
    is DiscoveryUiState.ScanError           -> BuildTone.Failure
}
