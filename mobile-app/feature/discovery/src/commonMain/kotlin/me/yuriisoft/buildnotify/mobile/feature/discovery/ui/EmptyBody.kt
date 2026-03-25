package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.resource.RawText
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun EmptyBody() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = RawText("No devices found"),
                style = BuildNotifyTheme.typography.titleMedium,
                color = BuildNotifyTheme.colors.content.primary,
            )

            Spacer(Modifier.height(BuildNotifyTheme.dimensions.spacing.small))

            Text(
                text = RawText("Make sure the Build Notify plugin is running in your IDE"),
                style = BuildNotifyTheme.typography.bodyMedium,
                color = BuildNotifyTheme.colors.content.secondary,
            )
        }
    }
}