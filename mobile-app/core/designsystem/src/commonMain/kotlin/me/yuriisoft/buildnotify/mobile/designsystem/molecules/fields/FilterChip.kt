package me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Filter chip: "All 12", "Errors 4". Selected is a solid inverse pill. */
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val colors = GraphiteTheme.colors
    val content = if (selected) colors.onInverse else colors.contentSecondary
    Row(
        modifier = modifier
            .semantics { this.selected = selected }
            .styledClickable(rememberChipStyle(), onClick = onClick, selected = selected, role = Role.Tab),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.inline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = GraphiteTheme.typography.chip, color = content)
        if (count != null) Text(
            count.toString(),
            style = GraphiteTheme.typography.mono,
            color = content.copy(alpha = GraphiteTheme.opacity.secondaryContent),
        )
    }
}
