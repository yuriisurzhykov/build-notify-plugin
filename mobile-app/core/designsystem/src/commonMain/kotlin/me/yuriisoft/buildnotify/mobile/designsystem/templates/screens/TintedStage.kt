package me.yuriisoft.buildnotify.mobile.designsystem.templates.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.externalPaddingHorizontal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Full-bleed tinted stage behind a multi-step flow; the tint glides between tones. */
@Composable
internal fun rememberStageStyle(tone: BuildTone): Style {
    val hero = animateStatus(tone).hero
    val surfaceShape = GraphiteTheme.shapes.hero
    val inset = GraphiteTheme.spacing.heroInset
    return remember(hero, surfaceShape, inset) {
        Style {
            externalPaddingHorizontal(inset)
            shape(surfaceShape)
            background(hero)
        }
    }
}

/**
 * Hero-tinted area without padding, for multi-step flows whose steps swap inside it.
 * The tint glides when [tone] changes.
 */
@Composable
fun TintedStage(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.styled(rememberStageStyle(tone)), content = content)
}
