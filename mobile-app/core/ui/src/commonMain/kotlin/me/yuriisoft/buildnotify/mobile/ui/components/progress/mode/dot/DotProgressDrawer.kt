package me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.dot

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

@Stable
fun interface DotProgressDrawer {
    fun DrawScope.draw(color: Color, dotCount: Int, dotRadius: Float, spacing: Float)
}
