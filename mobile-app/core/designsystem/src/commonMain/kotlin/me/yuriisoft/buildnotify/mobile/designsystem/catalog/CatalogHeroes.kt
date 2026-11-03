package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.feedback.ProgressBar
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_hand_off
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_log
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_stop
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_hero_build_failed
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_hero_build_running
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_hero_not_built
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_minutes_ago
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_more_errors
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_hero_failed
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_hero_running
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.console.LogTone
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.console.Console
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.InsetPanel
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.text.TextValue
import me.yuriisoft.buildnotify.mobile.designsystem.particles.text.joined
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FailedHeroSection() {
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography
    Section(stringResource(Res.string.catalog_section_hero_failed), padded = false) {
        HeroPanel(BuildTone.Failure) {
            Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.group)) {
                StatusLabel(
                    BuildTone.Failure,
                    TextValue.join(
                        listOf(
                            TextValue.of(Res.string.catalog_hero_build_failed),
                            TextValue.of(Res.string.catalog_minutes_ago, CatalogSamples.MINUTES_AGO),
                        ),
                        separator = TextValue.DotSeparator,
                    ).string(),
                )
                Text(TextValue.of(Res.string.catalog_hero_not_built, CatalogSamples.PAYMENT_SDK), style = type.display)
                Text(
                    listOf(CatalogSamples.TASK_COMPILE_PAYMENT, CatalogSamples.DURATION_FAILED)
                        .map(TextValue::of)
                        .joined(TextValue.DotSeparator),
                    style = type.mono,
                    color = colors.contentSecondary,
                )
            }
            InsetPanel {
                IssueText(CatalogSamples.ERROR_UNRESOLVED, CatalogSamples.ERROR_LOCATION)
                Text(
                    TextValue.of(
                        Res.plurals.catalog_more_errors,
                        CatalogSamples.MORE_ERRORS,
                        CatalogSamples.MORE_ERRORS,
                    ),
                    style = type.meta,
                    color = colors.contentSecondary,
                )
            }
            PrimaryButton(
                stringResource(Res.string.catalog_action_hand_off),
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = GraphiteIcons.ArrowRight,
            )
        }
    }
}

@Composable
internal fun RunningHeroSection() {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography
    Section(stringResource(Res.string.catalog_section_hero_running), padded = false) {
        HeroPanel(BuildTone.Running) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.inline)) {
                StatusLabel(BuildTone.Running, stringResource(Res.string.catalog_hero_build_running))
                Text(CatalogSamples.ELAPSED, style = type.timer)
                Text(
                    listOf(CatalogSamples.SHOP_ANDROID, CatalogSamples.TASK_ASSEMBLE_SHORT)
                        .map(TextValue::of)
                        .joined(TextValue.DotSeparator),
                    style = type.headline,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(CatalogSamples.TASK_COMPILE_APP, style = type.mono, color = colors.running.main)
                ProgressBar(progress = null)
            }
            Console(translucent = true) {
                LogLine(CatalogSamples.LOG_MERGE, tone = LogTone.Muted, singleLine = true)
                LogLine(CatalogSamples.LOG_KAPT, tone = LogTone.Muted, singleLine = true)
                LogLine(CatalogSamples.LOG_UNUSED, tone = LogTone.Warning, singleLine = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
                DangerButton(
                    stringResource(Res.string.catalog_action_stop),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    leadingIcon = GraphiteIcons.Stop,
                )
                SecondaryButton(
                    stringResource(Res.string.catalog_action_log),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    leadingIcon = GraphiteIcons.Terminal,
                )
            }
        }
    }
}
