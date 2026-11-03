package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

@Composable
internal fun HeroHint(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        style = GraphiteTheme.typography.caption,
        color = GraphiteTheme.colors.contentSecondary,
        textAlign = TextAlign.Center,
    )
}
