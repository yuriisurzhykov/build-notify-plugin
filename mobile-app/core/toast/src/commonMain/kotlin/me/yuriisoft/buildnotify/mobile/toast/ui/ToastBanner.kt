package me.yuriisoft.buildnotify.mobile.toast.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import me.yuriisoft.buildnotify.mobile.toast.ui.animation.auroraBackground
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.ui.components.icon.Icon
import me.yuriisoft.buildnotify.mobile.ui.theme.color.semantic.StatusRole

private val MinContentHeight = 48.dp

@Composable
internal fun ToastBanner(
    data: ToastData,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val statusRole = data.type.toStatusRole()
    val contentInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(GraphiteTheme.shapes.card)
            .auroraBackground(statusRole),
    ) {
        Row(
            modifier = contentModifier
                .fillMaxWidth()
                .windowInsetsPadding(contentInsets)
                .defaultMinSize(minHeight = MinContentHeight)
                .padding(
                    start = GraphiteTheme.spacing.l,
                    end = GraphiteTheme.spacing.l,
                    bottom = GraphiteTheme.spacing.s,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.message.resolve().toString(),
                style = GraphiteTheme.typography.bodyStrong,
                color = statusRole.onMain,
                textAlign = TextAlign.Center,
            )
            data.icon?.let { iconResource ->
                Icon(
                    modifier = Modifier.size(GraphiteTheme.sizes.icon),
                    contentDescription = null,
                    image = iconResource,
                    tint = statusRole.onMain,
                )
            }
        }
    }
}

/**
 * Graphite tone colours in the shape the aurora animation expects, so the animated
 * background keeps working while the banner follows the Graphite palette.
 */
@Composable
private fun ToastType.toStatusRole(): StatusRole {
    val tone = GraphiteTheme.colors.tone(
        when (this) {
            ToastType.Info    -> BuildTone.Running
            ToastType.Success -> BuildTone.Success
            ToastType.Warning -> BuildTone.Warning
            ToastType.Error   -> BuildTone.Failure
        },
    )
    return StatusRole(
        main = tone.main,
        onMain = tone.onMain,
        container = tone.container,
        onContainer = tone.main,
    )
}
