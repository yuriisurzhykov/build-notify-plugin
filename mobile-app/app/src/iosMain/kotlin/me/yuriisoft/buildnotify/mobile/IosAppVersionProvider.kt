package me.yuriisoft.buildnotify.mobile

import me.yuriisoft.buildnotify.mobile.domain.model.AppVersionProvider
import platform.Foundation.NSBundle

/**
 * Reads the version from the iOS bundle's `CFBundleShortVersionString`.
 */
class IosAppVersionProvider : AppVersionProvider {

    override val versionName: String =
        NSBundle.mainBundle.infoDictionary
            ?.get("CFBundleShortVersionString") as? String
            ?: "unknown"
}
