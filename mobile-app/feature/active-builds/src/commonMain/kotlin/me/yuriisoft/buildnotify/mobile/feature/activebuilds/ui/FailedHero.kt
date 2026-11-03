package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.Button
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.InsetPanel
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.shakeOn
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_handoff
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_failed_label
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_failed_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_handoff_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_more_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_no_errors
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.formatDuration
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FailedHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    val finishedAt = build.startedAt + build.durationMs

    HeroPanel(BuildTone.Failure) {
        Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.group)) {
            StatusLabel(
                BuildTone.Failure,
                stringResource(Res.string.hero_failed_label, relativeTime(finishedAt)),
                Modifier.shakeOn(build.buildId),
            )
            Text(stringResource(Res.string.hero_failed_title, build.projectName), style = type.display)
            Text(formatDuration(build.durationMs), style = type.mono, color = colors.contentSecondary)
        }
        Reveal(1) { FailedHeroIssue(build) }
        Reveal(2) {
            Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
                PrimaryButton(
                    text = stringResource(Res.string.action_handoff),
                    onClick = { onIntent(ActiveBuildsIntent.OpenHandoff(build.buildId)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = GraphiteIcons.ArrowRight,
                )
                HeroHint(stringResource(Res.string.hero_handoff_hint))
            }
        }
        Reveal(3) {
            Button(
                text = stringResource(Res.string.action_logs),
                onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Outline,
                leadingIcon = GraphiteIcons.Terminal,
            )
        }
    }
}

/** The first compiler error on an inset panel, with a count of the rest. */
@Composable
private fun FailedHeroIssue(build: FinishedBuildUi) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    InsetPanel {
        val first = build.topErrors.firstOrNull()
        if (first == null) {
            Text(stringResource(Res.string.hero_no_errors), style = type.bodySmall, color = colors.contentSecondary)
            return@InsetPanel
        }
        IssueText(first.message, first.location)
        val more = build.errorCount - 1
        if (more > 0) {
            Text(
                stringResource(Res.string.hero_more_errors, more),
                modifier = Modifier.padding(top = GraphiteTheme.spacing.inline),
                style = type.meta,
                color = colors.contentSecondary,
            )
        }
    }
}
