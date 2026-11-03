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
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.animateStatus
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** The hero panel; its tint glides between tones through [animateStatus]. */
@Composable
internal fun rememberHeroStyle(tone: BuildTone): Style {
    val hero = animateStatus(tone).hero
    val surfaceShape = GraphiteTheme.shapes.hero
    val inset = GraphiteTheme.spacing.heroInset
    val surface = GraphiteTheme.dimens.surface
    return remember(hero, surfaceShape, inset, surface) {
        Style {
            externalPaddingHorizontal(inset)
            shape(surfaceShape)
            background(hero)
            contentPaddingHorizontal(surface.heroPaddingHorizontal)
            contentPaddingTop(surface.heroPaddingTop)
            contentPaddingBottom(surface.heroPaddingBottom)
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
    val surface = GraphiteTheme.dimens.surface
    val insetAlpha = GraphiteTheme.opacity.inset
    return remember(colors, surfaceShape, padding, surface, insetAlpha) {
        Style {
            shape(surfaceShape)
            background(colors.console.copy(alpha = insetAlpha))
            contentPaddingHorizontal(padding)
            contentPaddingVertical(surface.insetPaddingVertical)
        }
    }
}

@Composable
internal fun rememberSheetStyle(): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.sheet
    val surface = GraphiteTheme.dimens.surface
    return remember(colors, surfaceShape, surface) {
        Style {
            shape(surfaceShape)
            background(colors.surface)
            contentPaddingHorizontal(surface.sheetPaddingHorizontal)
            contentPaddingTop(surface.sheetPaddingTop)
            contentPaddingBottom(surface.sheetPaddingBottom)
        }
    }
}

@Composable
internal fun rememberSheetHandleStyle(): Style {
    val colors = GraphiteTheme.colors
    val pill = GraphiteTheme.shapes.pill
    val surface = GraphiteTheme.dimens.surface
    return remember(colors, pill, surface) {
        Style {
            size(surface.sheetHandleWidth, surface.sheetHandleHeight)
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
