package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.ChoiceCard
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.console.Console
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.SettingRow
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.Switch
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.crossfade
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.smoothSize
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
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
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.HandoffUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HandoffHeader(handoff: HandoffUi) {
    Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs)) {
        Text(stringResource(Res.string.handoff_title), style = GraphiteTheme.typography.headline)
        Text(
            stringResource(Res.string.handoff_subtitle, handoff.projectName, handoff.errorCount),
            style = GraphiteTheme.typography.bodySmall,
            color = GraphiteTheme.colors.contentSecondary,
        )
    }
}

@Composable
internal fun HandoffTargetPicker(selected: HandoffTarget, onSelect: (HandoffTarget) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
        ChoiceCard(
            title = stringResource(Res.string.handoff_share),
            hint = stringResource(Res.string.handoff_share_hint),
            selected = selected == HandoffTarget.Share,
            onClick = { onSelect(HandoffTarget.Share) },
            modifier = Modifier.weight(1f),
        )
        ChoiceCard(
            title = stringResource(Res.string.handoff_copy),
            hint = stringResource(Res.string.handoff_copy_hint),
            selected = selected == HandoffTarget.Copy,
            onClick = { onSelect(HandoffTarget.Copy) },
            modifier = Modifier.weight(1f),
        )
    }
}

/** The exact message the agent will receive; grows smoothly when the log is attached. */
@Composable
internal fun HandoffPreview(prompt: String) {
    Column {
        SectionHeader(stringResource(Res.string.handoff_preview))
        Console(Modifier.smoothSize()) {
            Box(Modifier.heightIn(max = PreviewMaxHeight).verticalScroll(rememberScrollState())) {
                Text(prompt, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
internal fun HandoffAttachLog(handoff: HandoffUi, onToggle: () -> Unit) {
    SettingRow(
        title = stringResource(Res.string.handoff_attach_log),
        subtitle = stringResource(Res.string.handoff_attach_log_hint, handoff.logLineCount),
        onClick = onToggle,
    ) {
        Switch(
            checked = handoff.includeLog,
            onCheckedChange = { onToggle() },
            stateDescriptionOn = stringResource(Res.string.switch_on),
            stateDescriptionOff = stringResource(Res.string.switch_off),
        )
    }
}

/** Share or copy, depending on [target]; the button cross-fades when the target changes. */
@Composable
internal fun HandoffSendButton(
    target: HandoffTarget,
    prompt: String,
    onSent: () -> Unit,
    onCopied: () -> Unit,
) {
    val share = rememberShareText()
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    Swap(targetState = target, transition = { crossfade() }, label = "handoffCta") { current ->
        when (current) {
            HandoffTarget.Share -> PrimaryButton(
                text = stringResource(Res.string.handoff_send_share),
                onClick = {
                    share(prompt)
                    onSent()
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = GraphiteIcons.ArrowRight,
            )

            HandoffTarget.Copy  -> PrimaryButton(
                text = stringResource(Res.string.handoff_send_copy),
                onClick = {
                    clipboard.setText(AnnotatedString(prompt))
                    onSent()
                    onCopied()
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = GraphiteIcons.Copy,
            )
        }
    }
}

private val PreviewMaxHeight = 220.dp
