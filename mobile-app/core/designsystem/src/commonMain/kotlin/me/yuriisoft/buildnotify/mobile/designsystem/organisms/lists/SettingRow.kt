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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

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
