package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.ring

import androidx.compose.runtime.Immutable
import me.yuriisoft.buildnotify.mobile.ui.components.progress.ProgressDefaults

@Immutable
data class PulsingRingSpec(
    val ringCount: Int = ProgressDefaults.PulsingRingCount,
    val durationMillis: Int = ProgressDefaults.PulsingRingDurationMillis,
    val staggerDelayMillis: Int = ProgressDefaults.PulsingRingStaggerDelayMillis,
    val minAlpha: Float = ProgressDefaults.PulsingRingMinAlpha,
    val maxAlpha: Float = ProgressDefaults.PulsingRingMaxAlpha,
    val minScale: Float = ProgressDefaults.PulsingRingMinScale,
    val maxScale: Float = ProgressDefaults.PulsingRingMaxScale,
)
