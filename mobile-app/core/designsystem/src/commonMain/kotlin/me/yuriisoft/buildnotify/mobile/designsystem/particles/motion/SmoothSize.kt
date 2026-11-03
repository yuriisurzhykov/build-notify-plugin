package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Animates size changes of this element with the Graphite settle spring. */
@Composable
fun Modifier.smoothSize(): Modifier = animateContentSize(GraphiteTheme.motion.settle())
