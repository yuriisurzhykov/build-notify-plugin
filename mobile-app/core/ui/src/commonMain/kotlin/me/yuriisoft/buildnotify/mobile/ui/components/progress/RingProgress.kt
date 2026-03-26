package me.yuriisoft.buildnotify.mobile.ui.components.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.ring.RingProgressMode
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme
import me.yuriisoft.buildnotify.mobile.ui.theme.brush.semantic.GradientSpec

@Composable
fun RingProgress(
    modifier: Modifier = Modifier,
    mode: RingProgressMode = RingProgressMode.Pulsing(),
    gradient: GradientSpec = BuildNotifyTheme.brushes.progressGradient,
    size: Dp = BuildNotifyTheme.dimensions.component.ringProgressSize,
) {
    val drawer = mode.rememberDrawer()

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { progressBarRangeInfo = mode.rangeInfo },
    ) {
        with(drawer) { draw(gradient) }
    }
}
