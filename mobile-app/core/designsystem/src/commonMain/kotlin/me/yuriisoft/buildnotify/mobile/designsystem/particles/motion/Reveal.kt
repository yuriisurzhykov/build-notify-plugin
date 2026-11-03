package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Fades and lifts [content] in once, after `index ×` [GraphiteTheme.motion] stagger. Wrap the elements of a
 * screen in increasing indices to get a cascading entrance.
 */
@Composable
fun Reveal(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    val motion = GraphiteTheme.motion
    val delay = index * motion.staggerMillis
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(motion.enter(delay)) +
            slideInVertically(motion.enter<IntOffset>(delay)) { (it * motion.scales.revealLift).toInt() },
    ) {
        content()
    }
}
