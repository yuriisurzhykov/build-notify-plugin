package me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

/**
 * The hero: one flat panel at the top of the screen that answers
 * "is everything OK right now". Its tint follows [tone] and glides between tones.
 */
@Composable
fun HeroPanel(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.fillMaxWidth().styled(rememberHeroStyle(tone)),
            verticalArrangement = Arrangement.spacedBy(HeroGap),
            content = content,
        )
    }
}

private val HeroGap = 18.dp
