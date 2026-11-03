package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import build_notify_mobile.feature.discovery.generated.resources.Res
import build_notify_mobile.feature.discovery.generated.resources.action_retry
import build_notify_mobile.feature.discovery.generated.resources.error_title
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.shakeOn
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ErrorBody(message: String, onRetry: () -> Unit) {
    StateBody(
        visual = { Glyph(GraphiteIcons.Close, BuildTone.Failure, Modifier.shakeOn(Unit)) },
        title = stringResource(Res.string.error_title),
        titleColor = GraphiteTheme.colors.failure.main,
        body = message,
    ) {
        PrimaryButton(
            text = stringResource(Res.string.action_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Rerun,
        )
    }
}
