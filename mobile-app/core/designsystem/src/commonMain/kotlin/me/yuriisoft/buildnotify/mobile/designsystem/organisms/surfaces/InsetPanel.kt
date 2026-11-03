package me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Inner panel on a hero for the key detail (first error, prompt preview).
 * Translucent so the hero tint shows through.
 */
@Composable
fun InsetPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().styled(rememberInsetStyle()),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs),
        content = content,
    )
}
