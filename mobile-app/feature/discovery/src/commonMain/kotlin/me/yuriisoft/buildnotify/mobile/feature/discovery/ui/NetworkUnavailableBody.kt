package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.network_unavailable_body
import build_notify_mobile.feature.discovery.generated.resources.network_unavailable_title
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.breathing
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NetworkUnavailableBody() {
    StateBody(
        visual = { Glyph(GraphiteIcons.Info, BuildTone.Warning, Modifier.breathing()) },
        title = stringResource(Res.string.network_unavailable_title),
        body = stringResource(Res.string.network_unavailable_body),
    )
}
