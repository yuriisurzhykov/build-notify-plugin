package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.pairing_body
import build_notify_mobile.feature.discovery.generated.resources.pairing_confirm
import build_notify_mobile.feature.discovery.generated.resources.pairing_pin_label
import build_notify_mobile.feature.discovery.generated.resources.pairing_reject
import build_notify_mobile.feature.discovery.generated.resources.pairing_title
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.DigitTile
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.Button
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonSize
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.OutlineButton
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.PopIn
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.breathing
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import org.jetbrains.compose.resources.stringResource

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
    Row(
        modifier = Modifier.breathing(),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.inline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pin.forEachIndexed { index, digit ->
            if (index > 0 && index % PIN_GROUP_SIZE == 0) Spacer(Modifier.width(GraphiteTheme.spacing.group))
            PopIn(delayMillis = PIN_POP_DELAY_MS + index * PIN_POP_STAGGER_MS) {
                DigitTile(digit)
            }
        }
    }
}

private const val PIN_GROUP_SIZE = 3

private const val PIN_POP_DELAY_MS = 200

private const val PIN_POP_STAGGER_MS = 70
