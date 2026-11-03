package me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Compiler diagnostic: message plus a mono `File.kt:42` location. */
@Composable
fun IssueText(
    message: String,
    location: String?,
    modifier: Modifier = Modifier,
    locationColor: androidx.compose.ui.graphics.Color = GraphiteTheme.colors.failure.main,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xxs)) {
        Text(message, style = GraphiteTheme.typography.body)
        if (location != null) Text(location, style = GraphiteTheme.typography.log, color = locationColor)
    }
}
