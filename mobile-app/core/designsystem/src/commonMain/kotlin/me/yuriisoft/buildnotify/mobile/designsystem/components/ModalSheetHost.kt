package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Appear
import me.yuriisoft.buildnotify.mobile.designsystem.motion.AppearTransition
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/**
 * Modal bottom sheet: a scrim that dismisses on tap and [sheet] sliding in from the
 * bottom. Place it as the last child of the screen's root [Box].
 *
 * The last non-null [model] is remembered so the sheet keeps its content while it slides
 * out after [model] became `null`.
 *
 * @param dismissLabel spoken label of the scrim, e.g. "Close".
 */
@Composable
fun <T : Any> BoxScope.ModalSheetHost(
    model: T?,
    onDismiss: () -> Unit,
    dismissLabel: String,
    sheet: @Composable (model: T) -> Unit,
) {
    var last by remember { mutableStateOf(model) }
    if (model != null) last = model
    val visible = model != null

    Appear(visible = visible, transition = AppearTransition.Fade) {
        Box(
            Modifier
                .fillMaxSize()
                .background(GraphiteTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = dismissLabel,
                    role = Role.Button,
                    onClick = onDismiss,
                ),
        )
    }
    Appear(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        transition = AppearTransition.Sheet,
    ) {
        last?.let { sheet(it) }
    }
}
