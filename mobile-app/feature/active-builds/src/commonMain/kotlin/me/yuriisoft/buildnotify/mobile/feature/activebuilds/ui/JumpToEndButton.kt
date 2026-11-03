package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Appear
import me.yuriisoft.buildnotify.mobile.designsystem.motion.AppearTransition
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.logs_jump_to_end
import org.jetbrains.compose.resources.stringResource

/** Floating button that pops in while the reader is away from the end of the log. */
@Composable
internal fun JumpToEndButton(
    visible: Boolean,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Appear(
        visible = visible,
        modifier = modifier
            .navigationBarsPadding()
            .padding(GraphiteTheme.spacing.xl),
        transition = AppearTransition.Pop,
    ) {
        Button(
            text = stringResource(Res.string.logs_jump_to_end),
            onClick = { scope.launch { listState.scrollToEnd() } },
            variant = ButtonVariant.Secondary,
            leadingIcon = GraphiteIcons.ArrowDown,
        )
    }
}
