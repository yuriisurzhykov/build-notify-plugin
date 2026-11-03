package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Shows or hides [content] with one of the Graphite [AppearTransition]s. */
@Composable
fun Appear(
    visible: Boolean,
    modifier: Modifier = Modifier,
    transition: AppearTransition = AppearTransition.Fade,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val popScale = GraphiteTheme.motion.scales.appearPop
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = transition.enter(popScale),
        exit = transition.exit(popScale),
        content = content,
    )
}

private fun AppearTransition.enter(popScale: Float): EnterTransition = when (this) {
    AppearTransition.Fade  -> fadeIn()
    AppearTransition.Pop   -> fadeIn() + scaleIn(initialScale = popScale)
    AppearTransition.Sheet -> slideInVertically { it } + fadeIn()
}

private fun AppearTransition.exit(popScale: Float): ExitTransition = when (this) {
    AppearTransition.Fade  -> fadeOut()
    AppearTransition.Pop   -> fadeOut() + scaleOut(targetScale = popScale)
    AppearTransition.Sheet -> slideOutVertically { it } + fadeOut()
}
