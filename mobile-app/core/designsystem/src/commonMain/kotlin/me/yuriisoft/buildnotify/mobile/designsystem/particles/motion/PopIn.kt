package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Springs [content] from a reduced scale on first composition — for success and status badges. */
@Composable
fun PopIn(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val motion = GraphiteTheme.motion
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(motion.fast(delayMillis)) + scaleIn(motion.bouncy(), initialScale = motion.scales.popIn),
    ) {
        content()
    }
}
