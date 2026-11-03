package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.StatusStyle

private val PillPaddingStart = 8.dp

private val PillPaddingEnd = 10.dp

private val TagPaddingHorizontal = 6.dp

private val TagPaddingVertical = 1.dp

@Composable
internal fun rememberStatusPillStyle(tone: BuildTone): Style {
    val status = GraphiteTheme.status(tone)
    val pill = GraphiteTheme.shapes.pill
    val height = GraphiteTheme.sizes.pill
    return remember(status, pill, height) {
        Style {
            shape(pill)
            background(status.container)
            minHeight(height)
            contentPaddingStart(PillPaddingStart)
            contentPaddingEnd(PillPaddingEnd)
        }
    }
}

@Composable
internal fun rememberTagStyle(tone: BuildTone): Style {
    val status = GraphiteTheme.status(tone)
    val surfaceShape = GraphiteTheme.shapes.tag
    return remember(status, surfaceShape) {
        Style {
            shape(surfaceShape)
            background(status.container)
            contentPaddingHorizontal(TagPaddingHorizontal)
            contentPaddingVertical(TagPaddingVertical)
        }
    }
}

/** Filled circle in the status container colour: status badges and icon badges. */
@Composable
internal fun rememberToneCircleStyle(status: StatusStyle, diameter: Dp): Style {
    val pill = GraphiteTheme.shapes.pill
    return remember(status, pill, diameter) {
        Style {
            size(diameter)
            shape(pill)
            background(status.container)
        }
    }
}

/** Solid dot in the tone's main colour. */
@Composable
internal fun rememberStatusDotStyle(tone: BuildTone, diameter: Dp): Style {
    val status = GraphiteTheme.status(tone)
    val pill = GraphiteTheme.shapes.pill
    return remember(status, pill, diameter) {
        Style {
            size(diameter)
            shape(pill)
            background(status.main)
        }
    }
}
