package me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.layout.Divider
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** "Run again": a Gradle task with its usual duration and a run glyph. */
@Composable
fun RunTaskRow(
    task: String,
    estimate: String,
    onRun: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = GraphiteTheme.colors
    Column(modifier.fillMaxWidth().styledClickable(Style, onRun)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = GraphiteTheme.sizes.listRow),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                task,
                modifier = Modifier.weight(1f),
                style = GraphiteTheme.typography.code,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(estimate, style = GraphiteTheme.typography.caption, color = colors.contentTertiary)
            Icon(GraphiteIcons.Play, contentDescription = null, tint = colors.success.main)
        }
        if (showDivider) Divider(color = colors.content.copy(alpha = GraphiteTheme.opacity.divider))
    }
}
