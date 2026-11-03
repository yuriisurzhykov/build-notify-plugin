package me.yuriisoft.buildnotify.mobile.designsystem.molecules.status

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.contentPaddingVertical
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.StatusStyle

@Composable
internal fun rememberStatusPillStyle(tone: BuildTone): Style {
    val status = GraphiteTheme.status(tone)
    val pill = GraphiteTheme.shapes.pill
    val height = GraphiteTheme.sizes.pill
    val metrics = GraphiteTheme.dimens.status
    return remember(status, pill, height, metrics) {
        Style {
            shape(pill)
            background(status.container)
            minHeight(height)
            contentPaddingStart(metrics.pillPaddingStart)
            contentPaddingEnd(metrics.pillPaddingEnd)
        }
    }
}

@Composable
internal fun rememberTagStyle(tone: BuildTone): Style {
    val status = GraphiteTheme.status(tone)
    val surfaceShape = GraphiteTheme.shapes.tag
    val metrics = GraphiteTheme.dimens.status
    return remember(status, surfaceShape, metrics) {
        Style {
            shape(surfaceShape)
            background(status.container)
            contentPaddingHorizontal(metrics.tagPaddingHorizontal)
            contentPaddingVertical(metrics.tagPaddingVertical)
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
