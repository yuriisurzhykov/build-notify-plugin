package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.action_try_again
import build_notify_mobile.feature.discovery.generated.resources.connection_failed_title
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.shakeOn
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConnectionFailedBody(
    host: TextResource,
    reason: TextResource,
    onRetry: () -> Unit,
) {
    StateBody(
        visual = { Glyph(GraphiteIcons.Close, BuildTone.Failure, Modifier.shakeOn(Unit)) },
        title = stringResource(Res.string.connection_failed_title),
        titleColor = GraphiteTheme.colors.failure.main,
        body = host.resolve().toString(),
        detail = reason.resolve().toString(),
    ) {
        PrimaryButton(
            text = stringResource(Res.string.action_try_again),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Rerun,
        )
    }
}
