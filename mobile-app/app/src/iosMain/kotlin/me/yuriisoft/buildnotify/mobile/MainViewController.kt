package me.yuriisoft.buildnotify.mobile

import androidx.compose.ui.window.ComposeUIViewController
import me.yuriisoft.buildnotify.mobile.data.discovery.IosNsdDiscovery
import me.yuriisoft.buildnotify.mobile.data.platform.IosNetworkMonitor
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val component = AppComponent::class.create(
        IosNsdDiscovery(),
        IosNetworkMonitor(),
        IosAppVersionProvider(),
    )
    return ComposeUIViewController {
        App(screens = component.screens)
    }
}
