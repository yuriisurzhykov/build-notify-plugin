package me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.foundation.style.externalPaddingHorizontal
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** The hero panel; its tint glides between tones through [animateStatus]. */
@Composable
internal fun rememberHeroStyle(tone: BuildTone): Style {
    val hero = animateStatus(tone).hero
    val surfaceShape = GraphiteTheme.shapes.hero
    val inset = GraphiteTheme.spacing.heroInset
    return remember(hero, surfaceShape, inset) {
        Style {
            externalPaddingHorizontal(inset)
            shape(surfaceShape)
            background(hero)
            contentPaddingHorizontal(HeroPaddingHorizontal)
            contentPaddingTop(HeroPaddingTop)
            contentPaddingBottom(HeroPaddingBottom)
        }
    }
}

@Composable
internal fun rememberCardStyle(): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.card
    val sizes = GraphiteTheme.sizes
    val padding = GraphiteTheme.spacing.l
    return remember(colors, surfaceShape, sizes, padding) {
        Style {
            shape(surfaceShape)
            background(colors.surface)
            border(sizes.hairline, colors.line)
            contentPadding(padding)
        }
    }
}

/** Translucent inner panel on a hero, so the hero tint shows through. */
@Composable
internal fun rememberInsetStyle(): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.inset
    val padding = GraphiteTheme.spacing.l
    return remember(colors, surfaceShape, padding) {
        Style {
            shape(surfaceShape)
            background(colors.console.copy(alpha = InsetAlpha))
            contentPaddingHorizontal(padding)
            contentPaddingVertical(InsetPaddingVertical)
        }
    }
}

@Composable
internal fun rememberSheetStyle(): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.sheet
    return remember(colors, surfaceShape) {
        Style {
            shape(surfaceShape)
            background(colors.surface)
            contentPaddingHorizontal(SheetPaddingHorizontal)
            contentPaddingTop(SheetPaddingTop)
            contentPaddingBottom(SheetPaddingBottom)
        }
    }
}

@Composable
internal fun rememberSheetHandleStyle(): Style {
    val colors = GraphiteTheme.colors
    val pill = GraphiteTheme.shapes.pill
    return remember(colors, pill) {
        Style {
            size(36.dp, 4.dp)
            shape(pill)
            background(colors.lineStrong)
        }
    }
}

/*
 * Styles of every Graphite surface. A component composes layout; its look — background,
 * shape, border, padding, size — is one of these Styles. Change the look here, never at
 * the call site.
 */

private val HeroPaddingHorizontal = 20.dp

private val HeroPaddingTop = 24.dp

private val HeroPaddingBottom = 20.dp

private val InsetPaddingVertical = 14.dp

private val SheetPaddingHorizontal = 20.dp

private val SheetPaddingTop = 10.dp

private val SheetPaddingBottom = 28.dp

private const val InsetAlpha = 0.55f
