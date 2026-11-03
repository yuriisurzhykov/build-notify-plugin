package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.runtime.Composable
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.waiting_for_ide_body
import build_notify_mobile.feature.discovery.generated.resources.waiting_for_ide_title
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import org.jetbrains.compose.resources.stringResource

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
