package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.ring

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.DrawScope
import me.yuriisoft.buildnotify.mobile.ui.theme.brush.semantic.GradientSpec

@Stable
fun interface RingProgressDrawer {
    fun DrawScope.draw(gradient: GradientSpec)
}
