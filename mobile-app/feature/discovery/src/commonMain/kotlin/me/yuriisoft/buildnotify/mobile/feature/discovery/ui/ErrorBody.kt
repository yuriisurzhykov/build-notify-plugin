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
internal fun ErrorBody(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = RawText("Something went wrong"),
                style = BuildNotifyTheme.typography.titleMedium,
                color = BuildNotifyTheme.colors.status.error.main,
            )

            Spacer(Modifier.height(BuildNotifyTheme.dimensions.spacing.small))

            Text(
                text = RawText(message),
                style = BuildNotifyTheme.typography.bodyMedium,
                color = BuildNotifyTheme.colors.content.secondary,
            )
        }
    }
}