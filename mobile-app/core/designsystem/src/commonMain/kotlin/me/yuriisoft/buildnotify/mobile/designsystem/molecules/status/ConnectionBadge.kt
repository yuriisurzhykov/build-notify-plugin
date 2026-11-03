package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.status.StatusDot
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** "● MyProject · Android Studio" — connection state in the top bar. */
@Composable
fun ConnectionBadge(
    text: String,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(
            if (connected) BuildTone.Success else BuildTone.Neutral,
            size = GraphiteTheme.dimens.status.connectionDot,
        )
        Text(text, style = GraphiteTheme.typography.meta, color = GraphiteTheme.colors.contentSecondary, maxLines = 1)
    }
}
