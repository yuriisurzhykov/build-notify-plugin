package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.runtime.Composable
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.scanning_body
import build_notify_mobile.feature.discovery.generated.resources.scanning_title
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

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
