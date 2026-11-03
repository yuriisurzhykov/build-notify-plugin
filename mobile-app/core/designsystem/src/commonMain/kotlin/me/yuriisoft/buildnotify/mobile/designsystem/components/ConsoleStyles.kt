package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

private val LogPaddingHorizontal = 6.dp

private val LogPaddingVertical = 3.dp

private const val TranslucentConsoleAlpha = 0.6f

private const val LogHighlightAlpha = 0.08f

@Composable
internal fun rememberConsoleStyle(translucent: Boolean): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.inset
    val spacing = GraphiteTheme.spacing
    return remember(colors, surfaceShape, spacing, translucent) {
        Style {
            shape(surfaceShape)
            background(if (translucent) colors.console.copy(alpha = TranslucentConsoleAlpha) else colors.console)
            contentPaddingHorizontal(spacing.s)
            contentPaddingVertical(spacing.m)
        }
    }
}

/** A log line; error lines get a faint red highlight. */
@Composable
internal fun rememberLogLineStyle(highlighted: Boolean): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.log
    return remember(colors, surfaceShape, highlighted) {
        Style {
            shape(surfaceShape)
            background(if (highlighted) colors.consoleError.copy(alpha = LogHighlightAlpha) else Color.Transparent)
            contentPaddingHorizontal(LogPaddingHorizontal)
            contentPaddingVertical(LogPaddingVertical)
        }
    }
}

/** One row of a lazily rendered console: rounded only where the console begins or ends. */
@Composable
internal fun rememberConsoleSegmentStyle(position: ConsoleSegmentPosition): Style {
    val colors = GraphiteTheme.colors
    val padding = GraphiteTheme.spacing.s
    return remember(colors, padding, position) {
        val top = if (position.isFirst) ConsoleRadius else 0.dp
        val bottom = if (position.isLast) ConsoleRadius else 0.dp
        Style {
            shape(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
            background(colors.console)
            contentPaddingHorizontal(padding)
            contentPaddingTop(if (position.isFirst) ConsoleEdgePadding else 0.dp)
            contentPaddingBottom(if (position.isLast) ConsoleEdgePadding else 0.dp)
        }
    }
}

private val ConsoleRadius = 16.dp

private val ConsoleEdgePadding = 6.dp
