package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Horizontal shake whenever [trigger] changes — attention for failures and rejections. */
@Composable
fun Modifier.shakeOn(trigger: Any?): Modifier {
    val shake = GraphiteTheme.motion.shake
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger, shake) {
        if (trigger == null) return@LaunchedEffect
        offset.snapTo(0f)
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = shake.durationMillis
                shake.keyframes.forEach { (offsetDp, atMillis) -> offsetDp at atMillis }
            },
        )
    }
    return graphicsLayer { translationX = offset.value * density }
}
