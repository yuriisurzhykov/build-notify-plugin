package me.yuriisoft.buildnotify.mobile.toast.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.toast.ToastHostState
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.ui.animation.rememberPullRevealState
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

private val SwipeDismissThreshold = 56.dp
private const val SwipeAlphaReduction = 0.5f

/**
 * Top-level host that renders the currently active toast from [hostState]
 * with a pull-reveal entrance animation and optional swipe-to-dismiss.
 *
 * Place inside a `Box` aligned to `Alignment.TopCenter` above the
 * navigation graph so banners overlay all screens.
 */
@Composable
fun ToastHost(
    hostState: ToastHostState,
    modifier: Modifier = Modifier,
) {
    val currentToast = hostState.currentToast
    val revealState = rememberPullRevealState()
    var displayedToast by remember { mutableStateOf<ToastData?>(null) }

    LaunchedEffect(currentToast) {
        if (currentToast == displayedToast) return@LaunchedEffect
        if (displayedToast != null) revealState.exit()
        displayedToast = currentToast
        if (currentToast != null) {
            withFrameNanos { }
            revealState.enter()
        }
    }

    val toast = displayedToast ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                clip = true
                shape = revealState.shape
            },
    ) {
        key(toast.id) {
            SwipeDismissBanner(
                data = toast,
                onDismiss = { hostState.dismiss(toast.id) },
                modifier = Modifier.padding(
                    horizontal = BuildNotifyTheme.dimensions.spacing.regular,
                    vertical = BuildNotifyTheme.dimensions.spacing.small,
                ),
            )
        }
    }
}

/**
 * Wraps [ToastBanner] with an upward-swipe gesture for non-sticky types.
 *
 * Drag offset and alpha are applied via [graphicsLayer] so that frames
 * during the drag only trigger layer re-draws, not full recomposition.
 */
@Composable
private fun SwipeDismissBanner(
    data: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { SwipeDismissThreshold.toPx() } }

    val swipeModifier = if (!data.type.isSticky) {
        Modifier.pointerInput(data.id) {
            detectVerticalDragGestures(
                onDragEnd = {
                    if (dragOffsetY < -thresholdPx) {
                        onDismiss()
                    } else {
                        scope.launch {
                            animate(
                                initialValue = dragOffsetY,
                                targetValue = 0f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                            ) { value, _ -> dragOffsetY = value }
                        }
                    }
                },
                onDragCancel = { dragOffsetY = 0f },
                onVerticalDrag = { change, dragAmount ->
                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtMost(0f)
                    change.consume()
                },
            )
        }
    } else {
        Modifier
    }

    ToastBanner(
        data = data,
        modifier = modifier
            .graphicsLayer {
                translationY = dragOffsetY
                val progress = (dragOffsetY / -thresholdPx).coerceIn(0f, 1f)
                alpha = 1f - progress * SwipeAlphaReduction
            },
        contentModifier = swipeModifier,
    )
}
