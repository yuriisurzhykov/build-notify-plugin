package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.templates.sheets.ModalSheetHost
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.SheetSurface
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_close
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.HandoffUi
import org.jetbrains.compose.resources.stringResource

/** Bottom sheet that hands a failed build to the user's coding agent. */
@Composable
internal fun BoxScope.HandoffOverlay(
    handoff: HandoffUi?,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
) {
    val close = { onIntent(ActiveBuildsIntent.CloseHandoff) }
    ModalSheetHost(
        model = handoff,
        onDismiss = close,
        dismissLabel = stringResource(Res.string.action_close),
    ) { current ->
        HandoffSheet(current, onIntent, onCopied, close)
    }
}

@Composable
private fun HandoffSheet(
    handoff: HandoffUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
    close: () -> Unit,
) {
    var target by remember { mutableStateOf(HandoffTarget.Share) }
    SheetSurface {
        HandoffHeader(handoff)
        HandoffTargetPicker(selected = target, onSelect = { target = it })
        HandoffPreview(handoff.prompt)
        if (handoff.logLineCount > 0) {
            HandoffAttachLog(handoff, onToggle = { onIntent(ActiveBuildsIntent.ToggleHandoffLog) })
        }
        HandoffSendButton(target = target, prompt = handoff.prompt, onSent = close, onCopied = onCopied)
        Spacer(Modifier.navigationBarsPadding())
    }
}
