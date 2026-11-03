package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * Animated replacement of one content by another — the only entry point features use for
 * content transitions. Pick a [transition] from [GraphiteTransition].
 */
@Composable
fun <S> Swap(
    targetState: S,
    modifier: Modifier = Modifier,
    transition: GraphiteTransition<S> = GraphiteTransition.Crossfade,
    contentKey: (targetState: S) -> Any? = { it },
    label: String = "swap",
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val motion = GraphiteTheme.motion
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { contentTransform(transition, motion) },
        contentKey = contentKey,
        label = label,
        content = content,
    )
}
