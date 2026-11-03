package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.animateContentSize
import androidx.compose.ui.Modifier

/** Animates size changes of this element with the Graphite settle spring. */
fun Modifier.smoothSize(): Modifier = animateContentSize(GraphiteMotion.settle())
