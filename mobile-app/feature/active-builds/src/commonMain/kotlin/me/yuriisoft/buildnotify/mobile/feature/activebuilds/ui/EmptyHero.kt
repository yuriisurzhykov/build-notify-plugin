package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.HeroPanel
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_body
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.builds_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyHero() {
    HeroPanel(BuildTone.Neutral) {
        Reveal(0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.builds_empty_title), style = GraphiteTheme.typography.display)
                Text(
                    stringResource(Res.string.builds_empty_body),
                    style = GraphiteTheme.typography.bodySmall,
                    color = GraphiteTheme.colors.contentSecondary,
                )
            }
        }
    }
}
