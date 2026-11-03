package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.status.ConnectionBadge
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.GraphiteTransition
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.home_offline
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.home_reconnecting
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.ConnectionUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeTopBar(connection: ConnectionUi) {
    val text = when (connection) {
        is ConnectionUi.Connected -> connection.hostName
        is ConnectionUi.Reconnecting -> stringResource(Res.string.home_reconnecting)
        is ConnectionUi.Offline      -> stringResource(Res.string.home_offline)
    }
    Swap(
        targetState = text to (connection is ConnectionUi.Connected),
        modifier = Modifier.padding(horizontal = GraphiteTheme.spacing.screen, vertical = GraphiteTheme.spacing.l),
        transition = GraphiteTransition.Crossfade,
        label = "connection",
    ) { (label, connected) ->
        ConnectionBadge(text = label, connected = connected)
    }
}
