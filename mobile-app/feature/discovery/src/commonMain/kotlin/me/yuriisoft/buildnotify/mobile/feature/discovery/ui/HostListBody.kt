package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.Card
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.icons.Icon
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.status.StatusDot
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Reveal
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HostListBody(
    hosts: List<DiscoveredHost>,
    onHostSelected: (DiscoveredHost) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s),
    ) {
        itemsIndexed(hosts, key = { _, host -> "${host.host}:${host.port}" }) { index, host ->
            // animateItem: hosts that appear or vanish mid-scan slide and fade instead of jumping.
            Reveal(index, Modifier.animateItem()) {
                HostRow(host = host, onClick = { onHostSelected(host) })
            }
        }
    }
}

@Composable
private fun HostRow(host: DiscoveredHost, onClick: () -> Unit) {
    val colors = GraphiteTheme.colors
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(BuildTone.Success)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(host.name, style = GraphiteTheme.typography.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${host.host}:${host.port}",
                    style = GraphiteTheme.typography.mono,
                    color = colors.contentTertiary,
                    maxLines = 1,
                )
            }
            Icon(GraphiteIcons.ArrowRight, contentDescription = null, tint = colors.contentTertiary)
        }
    }
}
