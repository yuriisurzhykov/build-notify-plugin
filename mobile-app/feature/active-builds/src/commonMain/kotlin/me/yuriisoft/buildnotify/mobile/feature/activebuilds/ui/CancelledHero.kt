package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_cancelled_label
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_cancelled_title
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.FinishedBuildUi
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.formatDuration
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CancelledHero(build: FinishedBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val finishedAt = build.startedAt + build.durationMs
    HeroPanel(BuildTone.Warning) {
        Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.group)) {
            StatusLabel(BuildTone.Warning, stringResource(Res.string.hero_cancelled_label, relativeTime(finishedAt)))
            Text(
                stringResource(Res.string.hero_cancelled_title, build.projectName),
                style = GraphiteTheme.typography.display,
            )
            Text(
                formatDuration(build.durationMs),
                style = GraphiteTheme.typography.mono,
                color = GraphiteTheme.colors.contentSecondary,
            )
        }
        SecondaryButton(
            text = stringResource(Res.string.action_logs),
            onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = GraphiteIcons.Terminal,
        )
    }
}
