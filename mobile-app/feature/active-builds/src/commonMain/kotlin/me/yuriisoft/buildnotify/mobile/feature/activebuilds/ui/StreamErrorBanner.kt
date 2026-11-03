package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.Button
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.Card
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_retry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_stream_error
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StreamErrorBanner(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.builds_stream_error),
                modifier = Modifier.weight(1f),
                style = GraphiteTheme.typography.bodySmall,
                color = GraphiteTheme.colors.failure.main,
            )
            Button(
                text = stringResource(Res.string.action_retry),
                onClick = onRetry,
                variant = ButtonVariant.Outline,
                leadingIcon = GraphiteIcons.Rerun,
            )
        }
    }
}
