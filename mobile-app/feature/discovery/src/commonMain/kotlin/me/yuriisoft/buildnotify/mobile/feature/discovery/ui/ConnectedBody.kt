package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.runtime.Composable
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.connected_title
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.PopIn
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback.PulseRings
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import org.jetbrains.compose.resources.stringResource

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
