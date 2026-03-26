package me.yuriisoft.buildnotify.mobile

import androidx.compose.ui.window.ComposeUIViewController
import me.yuriisoft.buildnotify.mobile.feature.discovery.data.discovery.IosNsdDiscovery
import me.yuriisoft.buildnotify.mobile.tls.DarwinHttpClientProvider
import me.yuriisoft.buildnotify.mobile.tls.UserDefaultsTrustedServers
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val component = AppComponent::class.create(
        IosNsdDiscovery(),
        IosNetworkMonitor(),
        IosAppVersionProvider(),
        UserDefaultsTrustedServers(),
        DarwinHttpClientProvider(),
    )
    return ComposeUIViewController {
        App(screens = component.screens)
    }
}
