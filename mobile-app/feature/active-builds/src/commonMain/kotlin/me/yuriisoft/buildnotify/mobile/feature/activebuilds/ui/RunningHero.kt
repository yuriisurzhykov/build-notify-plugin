package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.Console
import me.yuriisoft.buildnotify.mobile.designsystem.components.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.components.LogLine
import me.yuriisoft.buildnotify.mobile.designsystem.components.ProgressBar
import me.yuriisoft.buildnotify.mobile.designsystem.components.RollingText
import me.yuriisoft.buildnotify.mobile.designsystem.components.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusLabel
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.motion.rise
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_logs
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.action_stop
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.build_preparing
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_running
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.hero_running_hint
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ActiveBuildsIntent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.RunningBuildUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RunningHero(build: RunningBuildUi, onIntent: (ActiveBuildsIntent) -> Unit) {
    val type = GraphiteTheme.typography
    val colors = GraphiteTheme.colors
    val elapsed = rememberElapsedMs(build.startedAt)

    HeroPanel(BuildTone.Running) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusLabel(BuildTone.Running, stringResource(Res.string.hero_running))
            RollingText(formatTimer(elapsed), style = type.timer)
            Text(build.projectName, style = type.headline)
        }
        Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
            Swap(targetState = build.currentTask, transition = { rise() }, label = "currentTask") { task ->
                Text(
                    task ?: stringResource(Res.string.build_preparing),
                    style = type.mono,
                    color = colors.running.main,
                    maxLines = 1,
                )
            }
            ProgressBar(progress = null)
        }
        if (build.recentLogs.isNotEmpty()) {
            Console(translucent = true) {
                build.recentLogs.forEach { line ->
                    LogLine(line.text, tone = line.kind.tone(), singleLine = true)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
            DangerButton(
                text = stringResource(Res.string.action_stop),
                onClick = { onIntent(ActiveBuildsIntent.StopBuild(build.buildId)) },
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Stop,
            )
            SecondaryButton(
                text = stringResource(Res.string.action_logs),
                onClick = { onIntent(ActiveBuildsIntent.OpenLogs(build.buildId)) },
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Terminal,
            )
        }
        HeroHint(stringResource(Res.string.hero_running_hint))
    }
}
