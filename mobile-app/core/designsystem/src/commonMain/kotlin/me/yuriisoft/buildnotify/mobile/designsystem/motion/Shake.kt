package me.yuriisoft.buildnotify.mobile.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Horizontal shake whenever [trigger] changes — attention for failures and rejections. */
@Composable
fun Modifier.shakeOn(trigger: Any?): Modifier {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        offset.snapTo(0f)
        offset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 420
                -14f at 60
                12f at 130
                -8f at 200
                5f at 270
                -2f at 340
            },
        )
    }
    return graphicsLayer { translationX = offset.value * density }
}
