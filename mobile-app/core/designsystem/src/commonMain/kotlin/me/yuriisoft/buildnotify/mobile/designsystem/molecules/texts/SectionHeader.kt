package me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Section title above a list: "Сегодня", "Запустить снова". */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(bottom = 6.dp),
        style = GraphiteTheme.typography.label,
        color = GraphiteTheme.colors.contentTertiary,
    )
}
