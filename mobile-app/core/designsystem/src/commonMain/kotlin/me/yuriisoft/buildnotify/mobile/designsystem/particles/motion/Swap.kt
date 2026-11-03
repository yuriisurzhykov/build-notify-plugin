package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Animated replacement of one content by another — the only entry point features use for
 * content transitions. Pick a [transition] from GraphiteTransitions (`crossfade`,
 * `morph`, `roll`, `rise`, `directional`).
 */
@Composable
fun <S> Swap(
    targetState: S,
    modifier: Modifier = Modifier,
    transition: AnimatedContentTransitionScope<S>.() -> ContentTransform = { crossfade() },
    contentKey: (targetState: S) -> Any? = { it },
    label: String = "swap",
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = transition,
        contentKey = contentKey,
        label = label,
        content = content,
    )
}
