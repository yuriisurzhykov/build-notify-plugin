package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.dot

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import me.yuriisoft.buildnotify.mobile.ui.components.progress.ProgressDefaults

@Immutable
data class BouncingDotSpec(
    val durationMillis: Int = ProgressDefaults.DotBounceDurationMillis,
    val staggerDelayMillis: Int = ProgressDefaults.DotStaggerDelayMillis,
    val bounceRatio: Float = ProgressDefaults.DotBounceRatio,
    val easing: Easing = ProgressDefaults.DotBounceEasing,
)
