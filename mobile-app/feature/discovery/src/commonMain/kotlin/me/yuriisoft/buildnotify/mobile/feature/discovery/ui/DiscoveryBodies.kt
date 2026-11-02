package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.action_cancel
import build_notify_mobile.feature.discovery.generated.resources.action_retry
import build_notify_mobile.feature.discovery.generated.resources.action_try_again
import build_notify_mobile.feature.discovery.generated.resources.connected_title
import build_notify_mobile.feature.discovery.generated.resources.connecting_title
import build_notify_mobile.feature.discovery.generated.resources.connection_failed_title
import build_notify_mobile.feature.discovery.generated.resources.empty_body
import build_notify_mobile.feature.discovery.generated.resources.empty_scan_again
import build_notify_mobile.feature.discovery.generated.resources.empty_title
import build_notify_mobile.feature.discovery.generated.resources.error_title
import build_notify_mobile.feature.discovery.generated.resources.idle_body
import build_notify_mobile.feature.discovery.generated.resources.idle_start_scan
import build_notify_mobile.feature.discovery.generated.resources.idle_title
import build_notify_mobile.feature.discovery.generated.resources.network_unavailable_body
import build_notify_mobile.feature.discovery.generated.resources.network_unavailable_title
import build_notify_mobile.feature.discovery.generated.resources.pairing_body
import build_notify_mobile.feature.discovery.generated.resources.pairing_confirm
import build_notify_mobile.feature.discovery.generated.resources.pairing_pin_label
import build_notify_mobile.feature.discovery.generated.resources.pairing_reject
import build_notify_mobile.feature.discovery.generated.resources.pairing_title
import build_notify_mobile.feature.discovery.generated.resources.scanning_body
import build_notify_mobile.feature.discovery.generated.resources.scanning_title
import build_notify_mobile.feature.discovery.generated.resources.waiting_for_ide_body
import build_notify_mobile.feature.discovery.generated.resources.waiting_for_ide_title
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonSize
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.Card
import me.yuriisoft.buildnotify.mobile.designsystem.components.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.components.OutlineButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusDot
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.components.VSpace
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.motion.PopIn
import me.yuriisoft.buildnotify.mobile.designsystem.motion.PulseRings
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.motion.breathing
import me.yuriisoft.buildnotify.mobile.designsystem.motion.shakeOn
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import org.jetbrains.compose.resources.stringResource

private val VisualSize: Dp = 168.dp

// region Building blocks

/**
 * Shared layout of every discovery state. Elements cascade in (visual → title → body →
 * detail → actions) each time the stage switches to a new state.
 */
@Composable
private fun StateBody(
    visual: @Composable () -> Unit,
    title: String,
    body: String? = null,
    detail: String? = null,
    titleColor: Color = Color.Unspecified,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Reveal(0) {
            Box(Modifier.size(VisualSize), contentAlignment = Alignment.Center) { visual() }
        }
        VSpace(12.dp)
        Reveal(1) {
            Text(title, style = type.headline, color = titleColor, textAlign = TextAlign.Center)
        }
        if (body != null) {
            VSpace(6.dp)
            Reveal(2) {
                Text(body, style = type.bodySmall, color = colors.contentSecondary, textAlign = TextAlign.Center)
            }
        }
        if (detail != null) {
            VSpace(6.dp)
            Reveal(3) {
                Text(detail, style = type.mono, color = colors.contentTertiary, textAlign = TextAlign.Center)
            }
        }
        VSpace(28.dp)
        Reveal(4) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
                content = actions,
            )
        }
    }
}

/** Round tone-tinted glyph at the centre of a state. */
@Composable
private fun Glyph(
    icon: ImageVector,
    tone: BuildTone,
    modifier: Modifier = Modifier,
) {
    val colors = GraphiteTheme.colors.tone(tone)
    Box(
        modifier = modifier
            .size(76.dp)
            .background(colors.container, GraphiteTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.main, size = 32.dp)
    }
}

/** Glyph inside radar rings — the app is waiting on the network or the IDE. */
@Composable
private fun WaitingGlyph(icon: ImageVector, tone: BuildTone) {
    PulseRings(color = GraphiteTheme.colors.tone(tone).main, size = VisualSize) {
        Glyph(icon, tone)
    }
}

@Composable
private fun CancelButton(onCancel: () -> Unit) {
    Button(
        text = stringResource(Res.string.action_cancel),
        onClick = onCancel,
        variant = ButtonVariant.Outline,
        modifier = Modifier.fillMaxWidth(0.6f),
    )
}

// endregion

@Composable
internal fun IdleBody(onStartScan: () -> Unit) {
    StateBody(
        visual = { Glyph(RadarIcon, BuildTone.Running, Modifier.breathing()) },
        title = stringResource(Res.string.idle_title),
        body = stringResource(Res.string.idle_body),
    ) {
        PrimaryButton(
            text = stringResource(Res.string.idle_start_scan),
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = GraphiteIcons.ArrowRight,
        )
    }
}

@Composable
internal fun ScanningBody(onCancel: () -> Unit) {
    StateBody(
        visual = { WaitingGlyph(HubIcon, BuildTone.Running) },
        title = stringResource(Res.string.scanning_title),
        body = stringResource(Res.string.scanning_body),
    ) {
        BouncingDots(color = GraphiteTheme.colors.running.main)
        CancelButton(onCancel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HostListBody(
    hosts: List<DiscoveredHost>,
    onHostSelected: (DiscoveredHost) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
    ) {
        itemsIndexed(hosts, key = { _, host -> "${host.host}:${host.port}" }) { index, host ->
            // animateItem: hosts that appear or vanish mid-scan slide and fade instead of jumping.
            Reveal(index, Modifier.animateItem()) {
                HostRow(host = host, onClick = { onHostSelected(host) })
            }
        }
    }
}

@Composable
private fun HostRow(host: DiscoveredHost, onClick: () -> Unit) {
    val colors = GraphiteTheme.colors
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(BuildTone.Success)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(host.name, style = GraphiteTheme.typography.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${host.host}:${host.port}", style = GraphiteTheme.typography.mono, color = colors.contentTertiary, maxLines = 1)
            }
            Icon(GraphiteIcons.ArrowRight, contentDescription = null, tint = colors.contentTertiary)
        }
    }
}

@Composable
internal fun PairingConfirmationBody(
    host: DiscoveredHost,
    pin: String,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
) {
    StateBody(
        visual = { PopIn { Glyph(ShieldIcon, BuildTone.Warning) } },
        title = stringResource(Res.string.pairing_title),
        body = host.name + "\n" + stringResource(Res.string.pairing_body),
    ) {
        Text(
            stringResource(Res.string.pairing_pin_label),
            style = GraphiteTheme.typography.label,
            color = GraphiteTheme.colors.contentTertiary,
        )
        PinDigits(pin)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = GraphiteTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        ) {
            OutlineButton(
                text = stringResource(Res.string.pairing_reject),
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            Button(
                text = stringResource(Res.string.pairing_confirm),
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Primary,
                size = ButtonSize.Regular,
            )
        }
    }
}

/** The PIN, one tile per digit: tiles pop in one after another, then the row breathes. */
@Composable
private fun PinDigits(pin: String) {
    val colors = GraphiteTheme.colors
    Row(
        modifier = Modifier.breathing(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pin.forEachIndexed { index, digit ->
            if (index > 0 && index % PIN_GROUP_SIZE == 0) Box(Modifier.size(width = 10.dp, height = 1.dp))
            PopIn(delayMillis = 200 + index * 70) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 52.dp)
                        .background(colors.surfaceRaised, GraphiteTheme.shapes.control),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        digit.toString(),
                        style = GraphiteTheme.typography.timer.copy(
                            fontSize = GraphiteTheme.typography.headline.fontSize * 1.2f,
                            lineHeight = GraphiteTheme.typography.headline.lineHeight,
                        ),
                    )
                }
            }
        }
    }
}

private const val PIN_GROUP_SIZE = 3

@Composable
internal fun WaitingForIdeBody(host: DiscoveredHost, onCancel: () -> Unit) {
    StateBody(
        visual = { WaitingGlyph(ShieldIcon, BuildTone.Warning) },
        title = stringResource(Res.string.waiting_for_ide_title),
        body = stringResource(Res.string.waiting_for_ide_body, host.name),
    ) {
        BouncingDots(color = GraphiteTheme.colors.warning.main)
        CancelButton(onCancel)
    }
}

@Composable
internal fun ConnectingBody(host: DiscoveredHost, onCancel: () -> Unit) {
    StateBody(
        visual = { WaitingGlyph(LinkIcon, BuildTone.Running) },
        title = stringResource(Res.string.connecting_title, host.name),
        detail = "${host.host}:${host.port}",
    ) {
        BouncingDots(color = GraphiteTheme.colors.running.main)
        CancelButton(onCancel)
    }
}

@Composable
internal fun ConnectedBody(host: DiscoveredHost) {
    StateBody(
        visual = {
            PulseRings(color = GraphiteTheme.colors.success.main, size = VisualSize, rings = 2) {
                PopIn { Glyph(GraphiteIcons.Check, BuildTone.Success) }
            }
        },
        title = stringResource(Res.string.connected_title, host.name),
        detail = "${host.host}:${host.port}",
    )
}

@Composable
internal fun ConnectionFailedBody(
    host: TextResource,
    reason: TextResource,
    onRetry: () -> Unit,
) {
    StateBody(
        visual = { Glyph(GraphiteIcons.Close, BuildTone.Failure, Modifier.shakeOn(Unit)) },
        title = stringResource(Res.string.connection_failed_title),
        titleColor = GraphiteTheme.colors.failure.main,
        body = host.resolve().toString(),
        detail = reason.resolve().toString(),
    ) {
        PrimaryButton(
            text = stringResource(Res.string.action_try_again),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Rerun,
        )
    }
}

@Composable
internal fun EmptyBody(onRetry: () -> Unit) {
    StateBody(
        visual = { Glyph(RadarIcon, BuildTone.Warning) },
        title = stringResource(Res.string.empty_title),
        body = stringResource(Res.string.empty_body),
    ) {
        Button(
            text = stringResource(Res.string.empty_scan_again),
            onClick = onRetry,
            variant = ButtonVariant.Secondary,
            leadingIcon = GraphiteIcons.Rerun,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun ErrorBody(message: String, onRetry: () -> Unit) {
    StateBody(
        visual = { Glyph(GraphiteIcons.Close, BuildTone.Failure, Modifier.shakeOn(Unit)) },
        title = stringResource(Res.string.error_title),
        titleColor = GraphiteTheme.colors.failure.main,
        body = message,
    ) {
        PrimaryButton(
            text = stringResource(Res.string.action_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Rerun,
        )
    }
}

@Composable
internal fun NetworkUnavailableBody() {
    StateBody(
        visual = { Glyph(GraphiteIcons.Info, BuildTone.Warning, Modifier.breathing()) },
        title = stringResource(Res.string.network_unavailable_title),
        body = stringResource(Res.string.network_unavailable_body),
    )
}
