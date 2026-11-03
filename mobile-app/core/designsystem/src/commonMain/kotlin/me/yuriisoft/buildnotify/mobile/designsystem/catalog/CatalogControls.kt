package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_back
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_copy_log
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_hand_off
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_log
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_rerun
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_settings
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_stop
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_unavailable
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_buttons
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_status
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_cancelled
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_failed
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_running
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_success
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_tag_agent
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.Button
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.IconButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.OutlineButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.ConnectionBadge
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusBadge
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.Tag
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StatusSection() {
    val spacing = GraphiteTheme.spacing
    Section(stringResource(Res.string.catalog_section_status)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
            verticalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            StatusPill(BuildTone.Running, stringResource(Res.string.catalog_status_running))
            StatusPill(BuildTone.Success, stringResource(Res.string.catalog_status_success))
            StatusPill(BuildTone.Failure, stringResource(Res.string.catalog_status_failed))
            StatusPill(BuildTone.Warning, stringResource(Res.string.catalog_status_cancelled))
            Tag(stringResource(Res.string.catalog_tag_agent))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
            StatusBadge(BuildTone.Success, contentDescription = stringResource(Res.string.catalog_status_success))
            StatusBadge(BuildTone.Failure, contentDescription = stringResource(Res.string.catalog_status_failed))
            StatusBadge(BuildTone.Running, contentDescription = stringResource(Res.string.catalog_status_running))
        }
        ConnectionBadge(CatalogSamples.IDE_CONNECTION, connected = true)
    }
}

@Composable
internal fun ButtonsSection() {
    val spacing = GraphiteTheme.spacing
    Section(stringResource(Res.string.catalog_section_buttons)) {
        PrimaryButton(
            stringResource(Res.string.catalog_action_hand_off),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = GraphiteIcons.ArrowRight,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            OutlineButton(
                stringResource(Res.string.catalog_action_rerun),
                onClick = {},
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Rerun,
            )
            SecondaryButton(
                stringResource(Res.string.catalog_action_log),
                onClick = {},
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Terminal,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            DangerButton(
                stringResource(Res.string.catalog_action_stop),
                onClick = {},
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Stop,
            )
            Button(
                stringResource(Res.string.catalog_action_unavailable),
                onClick = {},
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary,
                enabled = false,
            )
        }
        IconButtonsRow()
    }
}

@Composable
private fun IconButtonsRow() {
    Row {
        IconButton(
            GraphiteIcons.ArrowBack,
            contentDescription = stringResource(Res.string.catalog_action_back),
            onClick = {},
        )
        IconButton(
            GraphiteIcons.Copy,
            contentDescription = stringResource(Res.string.catalog_action_copy_log),
            onClick = {},
        )
        IconButton(
            GraphiteIcons.Settings,
            contentDescription = stringResource(Res.string.catalog_action_settings),
            onClick = {},
        )
    }
}
