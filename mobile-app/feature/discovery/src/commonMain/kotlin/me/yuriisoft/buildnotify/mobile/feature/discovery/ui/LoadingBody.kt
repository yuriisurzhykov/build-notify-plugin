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
import me.yuriisoft.buildnotify.mobile.ui.components.progress.CircularProgress
import me.yuriisoft.buildnotify.mobile.ui.components.progress.mode.circular.CircularProgressMode
import me.yuriisoft.buildnotify.mobile.ui.resource.RawText
import me.yuriisoft.buildnotify.mobile.ui.theme.BuildNotifyTheme

@Composable
internal fun LoadingBody() {
    val spacing = BuildNotifyTheme.dimensions.spacing

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgress(mode = CircularProgressMode.Indeterminate())

            Spacer(Modifier.height(spacing.regular))

            Text(
                text = RawText("Searching for devices\u2026"),
                style = BuildNotifyTheme.typography.bodyMedium,
                color = BuildNotifyTheme.colors.content.secondary,
            )
        }
    }
}