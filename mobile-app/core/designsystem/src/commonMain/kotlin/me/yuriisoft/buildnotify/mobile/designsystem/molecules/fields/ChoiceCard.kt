package me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styledClickable
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Selectable option card for one-of-N choices with a hint, e.g. which agent to hand off to.
 */
@Composable
fun ChoiceCard(
    title: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GraphiteTheme.colors
    val content = if (selected) colors.onInverse else colors.content
    Column(
        modifier = modifier
            .semantics { this.selected = selected }
            .styledClickable(
                rememberChoiceCardStyle(),
                onClick = onClick,
                selected = selected,
                role = Role.RadioButton,
            ),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xxs),
    ) {
        Text(title, style = GraphiteTheme.typography.chip, color = content, maxLines = 1)
        Text(
            hint,
            style = GraphiteTheme.typography.caption,
            color = content.copy(alpha = GraphiteTheme.opacity.hint),
            maxLines = 1,
        )
    }
}
