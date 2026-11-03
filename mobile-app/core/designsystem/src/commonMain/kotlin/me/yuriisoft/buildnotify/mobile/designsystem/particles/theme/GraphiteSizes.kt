package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GraphiteSizes(
    /** Minimum touch target for anything tappable. */
    val touchTarget: Dp = 44.dp,
    val buttonRegular: Dp = 48.dp,
    val buttonLarge: Dp = 54.dp,
    val chip: Dp = 36.dp,
    val pill: Dp = 24.dp,
    val listRow: Dp = 52.dp,
    val statusDot: Dp = 8.dp,
    val iconSmall: Dp = 14.dp,
    val icon: Dp = 18.dp,
    val iconLarge: Dp = 22.dp,
    val progressTrack: Dp = 3.dp,
    val hairline: Dp = 1.dp,
)
