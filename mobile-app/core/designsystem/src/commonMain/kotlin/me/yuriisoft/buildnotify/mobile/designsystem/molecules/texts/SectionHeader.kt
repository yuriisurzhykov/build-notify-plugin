package me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Section title above a list: "Today", "Run again". */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(bottom = GraphiteTheme.dimens.surface.sectionHeaderBottom),
        style = GraphiteTheme.typography.label,
        color = GraphiteTheme.colors.contentTertiary,
    )
}
