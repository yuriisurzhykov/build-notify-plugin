package me.yuriisoft.buildnotify.mobile.ui.components.progress

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.dot.DotProgressMode
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
fun DotProgress(
    modifier: Modifier = Modifier,
    mode: DotProgressMode = DotProgressMode.Bouncing(),
    color: Color = BuildNotifyTheme.colors.primary.main,
    dotCount: Int = ProgressDefaults.DotCount,
    dotSize: Dp = BuildNotifyTheme.dimensions.component.dotProgressDotSize,
    spacing: Dp = BuildNotifyTheme.dimensions.component.dotProgressSpacing,
) {
    val drawer = mode.rememberDrawer()
    val dotRadiusPx = with(LocalDensity.current) { (dotSize / 2).toPx() }
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }

    Canvas(
        modifier = modifier
            .semantics { progressBarRangeInfo = mode.rangeInfo },
    ) {
        with(drawer) { draw(color, dotCount, dotRadiusPx, spacingPx) }
    }
}
