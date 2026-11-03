package me.yuriisoft.buildnotify.mobile.designsystem.motion

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

/** Shows or hides [content] with one of the Graphite [AppearTransition]s. */
@Composable
fun Appear(
    visible: Boolean,
    modifier: Modifier = Modifier,
    transition: AppearTransition = AppearTransition.Fade,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = transition.enter(),
        exit = transition.exit(),
        content = content,
    )
}

private fun AppearTransition.enter(): EnterTransition = when (this) {
    AppearTransition.Fade  -> fadeIn()
    AppearTransition.Pop   -> fadeIn() + scaleIn(initialScale = POP_SCALE)
    AppearTransition.Sheet -> slideInVertically { it } + fadeIn()
}

private fun AppearTransition.exit(): ExitTransition = when (this) {
    AppearTransition.Fade  -> fadeOut()
    AppearTransition.Pop   -> fadeOut() + scaleOut(targetScale = POP_SCALE)
    AppearTransition.Sheet -> slideOutVertically { it } + fadeOut()
}

private const val POP_SCALE = 0.8f
