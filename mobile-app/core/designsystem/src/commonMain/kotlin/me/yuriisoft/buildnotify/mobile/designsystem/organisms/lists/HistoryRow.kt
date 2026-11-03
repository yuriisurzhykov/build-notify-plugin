package me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.layout.Divider
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.status.StatusDot
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.Tag
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * One build in the history: dot · title / (tag) subtitle · duration · time.
 * Plain row, no card — history is secondary to the hero.
 *
 * @param statusDescription spoken status for the dot, e.g. "Успешно".
 */
@Composable
fun HistoryRow(
    tone: BuildTone,
    statusDescription: String,
    title: String,
    subtitle: String,
    duration: String,
    time: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = GraphiteTheme.colors
    val typography = GraphiteTheme.typography
    val click = if (onClick != null) Modifier.styledClickable(Style, onClick) else Modifier

    Column(modifier.fillMaxWidth().then(click)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = GraphiteTheme.sizes.listRow),
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(tone, contentDescription = statusDescription)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, style = typography.bodyStrong, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (tag != null) Tag(tag)
                    Text(
                        subtitle,
                        style = typography.caption,
                        color = colors.contentTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(duration, style = typography.mono, color = colors.contentSecondary)
            Text(
                time,
                modifier = Modifier.width(40.dp),
                style = typography.log,
                color = colors.contentTertiary,
                textAlign = TextAlign.End,
            )
        }
        if (showDivider) Divider()
    }
}
