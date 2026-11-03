package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.idle_body
import build_notify_mobile.feature.discovery.generated.resources.idle_start_scan
import build_notify_mobile.feature.discovery.generated.resources.idle_title
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.breathing
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun IdleBody(onStartScan: () -> Unit) {
    StateBody(
        visual = { Glyph(RadarIcon, BuildTone.Running, Modifier.breathing()) },
        title = stringResource(Res.string.idle_title),
        body = stringResource(Res.string.idle_body),
    ) {
        PrimaryButton(
            text = stringResource(Res.string.idle_start_scan),
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = GraphiteIcons.ArrowRight,
        )
    }
}
