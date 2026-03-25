package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.domain.model.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.StatusDot
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.components.layout.Surface
import me.yuriisoft.buildnotify.mobile.ui.resource.RawText
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun HostItem(
    host: DiscoveredHost,
    onClick: () -> Unit,
) {
    val spacing = BuildNotifyTheme.dimensions.spacing

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = BuildNotifyTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.regular),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(color = BuildNotifyTheme.colors.status.success.main)

            Spacer(Modifier.width(spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = RawText(host.name),
                    style = BuildNotifyTheme.typography.titleMedium,
                    color = BuildNotifyTheme.colors.content.primary,
                    maxLines = 1,
                )

                Spacer(Modifier.height(spacing.xxSmall))

                Text(
                    text = RawText("${host.host}:${host.port}"),
                    style = BuildNotifyTheme.typography.bodySmall,
                    color = BuildNotifyTheme.colors.content.secondary,
                    maxLines = 1,
                )
            }
        }
    }
}