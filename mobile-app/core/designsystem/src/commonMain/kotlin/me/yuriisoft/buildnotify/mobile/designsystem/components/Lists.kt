package me.yuriisoft.buildnotify.mobile.designsystem.components

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

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

/** "Запустить снова": a Gradle task with its usual duration and a run glyph. */
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
            Text(task, modifier = Modifier.weight(1f), style = GraphiteTheme.typography.code, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(estimate, style = GraphiteTheme.typography.caption, color = colors.contentTertiary)
            Icon(GraphiteIcons.Play, contentDescription = null, tint = colors.success.main)
        }
        if (showDivider) Divider(color = colors.content.copy(alpha = 0.08f))
    }
}

/**
 * Two-line labelled row with a trailing control, e.g. a [Switch].
 * The whole row toggles; pass the same handler to the control.
 */
@Composable
fun SettingRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GraphiteTheme.sizes.touchTarget)
            .styledClickable(Style, onClick, role = Role.Switch),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = GraphiteTheme.typography.bodyStrong)
            if (subtitle != null) {
                Text(subtitle, style = GraphiteTheme.typography.caption, color = GraphiteTheme.colors.contentTertiary)
            }
        }
        trailing()
    }
}
