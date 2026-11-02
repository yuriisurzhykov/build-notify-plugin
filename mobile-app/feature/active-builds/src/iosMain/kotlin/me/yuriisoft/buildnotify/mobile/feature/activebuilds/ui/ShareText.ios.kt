package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
internal actual fun rememberShareText(): (text: String) -> Unit = remember {
    { text ->
        val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(sheet, animated = true, completion = null)
    }
}
