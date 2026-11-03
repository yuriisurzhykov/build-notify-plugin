package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusBadge
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.PopIn
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_issue_counts
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_success_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.formatDuration
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SuccessHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val finishedAt = build.startedAt + build.durationMs

    HeroPanel(BuildTone.Success) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PopIn { StatusBadge(BuildTone.Success) }
            Text(stringResource(Res.string.hero_success_title), style = type.display)
            Text(
                "${build.projectName} · ${formatDuration(build.durationMs)} · ${relativeTime(finishedAt)}",
                style = type.bodySmall,
                color = GraphiteTheme.colors.contentSecondary,
            )
            if (build.warningCount > 0) {
                Text(
                    stringResource(Res.string.build_issue_counts, 0, build.warningCount),
                    style = type.meta,
                    color = GraphiteTheme.colors.warning.main,
                )
            }
        }
        SecondaryButton(
            text = stringResource(Res.string.action_logs),
            onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Terminal,
        )
    }
}
