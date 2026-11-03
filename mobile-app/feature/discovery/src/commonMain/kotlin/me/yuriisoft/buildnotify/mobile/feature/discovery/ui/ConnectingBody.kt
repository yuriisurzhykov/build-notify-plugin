package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.runtime.Composable
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.connecting_title
import me.yuriisoft.buildnotify.mobile.designsystem.motion.BouncingDots
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import org.jetbrains.compose.resources.stringResource

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
