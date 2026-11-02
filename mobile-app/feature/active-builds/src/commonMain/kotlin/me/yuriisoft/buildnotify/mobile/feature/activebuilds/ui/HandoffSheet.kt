package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.ChoiceCard
import me.yuriisoft.buildnotify.mobile.designsystem.components.Console
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.components.SettingRow
import me.yuriisoft.buildnotify.mobile.designsystem.components.SheetSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.Switch
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_close
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_attach_log
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_attach_log_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_copy
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_copy_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_preview
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_send_copy
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_send_share
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_share
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_share_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_subtitle
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.handoff_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.switch_off
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.switch_on
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.HandoffUi
import org.jetbrains.compose.resources.stringResource

private enum class HandoffTarget { Share, Copy }

/**
 * Scrim + bottom sheet. Kept composed while sliding out so the sheet does not blank
 * mid-animation: the last non-null [handoff] is remembered.
 */
@Composable
internal fun BoxScope.HandoffOverlay(
    handoff: HandoffUi?,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
) {
    var last by remember { mutableStateOf(handoff) }
    if (handoff != null) last = handoff
    val visible = handoff != null
    val close = { onIntent(ActiveBuildsIntent.CloseHandoff) }
    val closeLabel = stringResource(Res.string.action_close)

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(GraphiteTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = closeLabel,
                    role = Role.Button,
                    onClick = close,
                ),
        )
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        last?.let { HandoffSheet(it, onIntent, onCopied, close) }
    }
}

@Composable
private fun HandoffSheet(
    handoff: HandoffUi,
    onIntent: (ActiveBuildsIntent) -> Unit,
    onCopied: () -> Unit,
    close: () -> Unit,
) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    var target by remember { mutableStateOf(HandoffTarget.Share) }
    val share = rememberShareText()
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    SheetSurface {
        Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs)) {
            Text(stringResource(Res.string.handoff_title), style = type.headline)
            Text(
                stringResource(Res.string.handoff_subtitle, handoff.projectName, handoff.errorCount),
                style = type.bodySmall,
                color = colors.contentSecondary,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
            ChoiceCard(
                title = stringResource(Res.string.handoff_share),
                hint = stringResource(Res.string.handoff_share_hint),
                selected = target == HandoffTarget.Share,
                onClick = { target = HandoffTarget.Share },
                modifier = Modifier.weight(1f),
            )
            ChoiceCard(
                title = stringResource(Res.string.handoff_copy),
                hint = stringResource(Res.string.handoff_copy_hint),
                selected = target == HandoffTarget.Copy,
                onClick = { target = HandoffTarget.Copy },
                modifier = Modifier.weight(1f),
            )
        }

        Column {
            SectionHeader(stringResource(Res.string.handoff_preview))
            Console(Modifier.animateContentSize()) {
                Box(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                    Text(handoff.prompt, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (handoff.logLineCount > 0) {
            val toggle = { onIntent(ActiveBuildsIntent.ToggleHandoffLog) }
            SettingRow(
                title = stringResource(Res.string.handoff_attach_log),
                subtitle = stringResource(Res.string.handoff_attach_log_hint, handoff.logLineCount),
                onClick = toggle,
            ) {
                Switch(
                    checked = handoff.includeLog,
                    onCheckedChange = { toggle() },
                    stateDescriptionOn = stringResource(Res.string.switch_on),
                    stateDescriptionOff = stringResource(Res.string.switch_off),
                )
            }
        }

        AnimatedContent(
            targetState = target,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "handoffCta",
        ) { current ->
            when (current) {
                HandoffTarget.Share -> PrimaryButton(
                    text = stringResource(Res.string.handoff_send_share),
                    onClick = {
                        share(handoff.prompt)
                        close()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = GraphiteIcons.ArrowRight,
                )

                HandoffTarget.Copy  -> PrimaryButton(
                    text = stringResource(Res.string.handoff_send_copy),
                    onClick = {
                        clipboard.setText(AnnotatedString(handoff.prompt))
                        close()
                        onCopied()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = GraphiteIcons.Copy,
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}
