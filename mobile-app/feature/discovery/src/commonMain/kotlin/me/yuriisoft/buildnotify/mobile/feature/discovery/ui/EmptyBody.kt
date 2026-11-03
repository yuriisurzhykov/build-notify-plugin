package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.empty_body
import build_notify_mobile.feature.discovery.generated.resources.empty_scan_again
import build_notify_mobile.feature.discovery.generated.resources.empty_title
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.Button
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import org.jetbrains.compose.resources.stringResource

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
