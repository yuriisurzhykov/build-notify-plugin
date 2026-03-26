package me.yuriisoft.buildnotify.mobile.feature.buildstatus.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.ui.components.foundation.Text
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource

@Composable
fun BuildStatusContent(modifier: Modifier = Modifier) {
    Text(
        text = TextResource.RawText("Build Status screen"),
    )
}