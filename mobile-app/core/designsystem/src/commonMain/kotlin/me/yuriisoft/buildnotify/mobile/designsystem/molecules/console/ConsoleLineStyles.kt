package me.yuriisoft.buildnotify.mobile.designsystem.molecules.console

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** A log line; error lines get a faint red highlight. */
@Composable
internal fun rememberLogLineStyle(highlighted: Boolean): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.log
    val console = GraphiteTheme.dimens.console
    val highlight = GraphiteTheme.opacity.logHighlight
    return remember(colors, surfaceShape, console, highlight, highlighted) {
        Style {
            shape(surfaceShape)
            background(if (highlighted) colors.consoleError.copy(alpha = highlight) else Color.Transparent)
            contentPaddingHorizontal(console.linePaddingHorizontal)
            contentPaddingVertical(console.linePaddingVertical)
        }
    }
}

/** One row of a lazily rendered console: rounded only where the console begins or ends. */
@Composable
internal fun rememberConsoleSegmentStyle(position: ConsoleSegmentPosition): Style {
    val colors = GraphiteTheme.colors
    val padding = GraphiteTheme.spacing.s
    val console = GraphiteTheme.dimens.console
    return remember(colors, padding, console, position) {
        val top = if (position.isFirst) console.cornerRadius else 0.dp
        val bottom = if (position.isLast) console.cornerRadius else 0.dp
        Style {
            shape(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
            background(colors.console)
            contentPaddingHorizontal(padding)
            contentPaddingTop(if (position.isFirst) console.edgePadding else 0.dp)
            contentPaddingBottom(if (position.isLast) console.edgePadding else 0.dp)
        }
    }
}
