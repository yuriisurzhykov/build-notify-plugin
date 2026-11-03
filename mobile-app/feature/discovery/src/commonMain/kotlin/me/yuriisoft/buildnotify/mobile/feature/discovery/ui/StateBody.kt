package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.action_cancel
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.IconBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.components.VSpace
import me.yuriisoft.buildnotify.mobile.designsystem.motion.PulseRings
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

internal val VisualSize: Dp = 168.dp


/**
 * Shared layout of every discovery state. Elements cascade in (visual → title → body →
 * detail → actions) each time the stage switches to a new state.
 */
@Composable
internal fun StateBody(
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
internal fun Glyph(
    icon: ImageVector,
    tone: BuildTone,
    modifier: Modifier = Modifier,
) {
    IconBadge(icon = icon, tone = tone, modifier = modifier)
}

/** Glyph inside radar rings — the app is waiting on the network or the IDE. */
@Composable
internal fun WaitingGlyph(icon: ImageVector, tone: BuildTone) {
    PulseRings(color = GraphiteTheme.status(tone).main, size = VisualSize) {
        Glyph(icon, tone)
    }
}

@Composable
internal fun CancelButton(onCancel: () -> Unit) {
    Button(
        text = stringResource(Res.string.action_cancel),
        onClick = onCancel,
        variant = ButtonVariant.Outline,
        modifier = Modifier.fillMaxWidth(0.6f),
    )
}

