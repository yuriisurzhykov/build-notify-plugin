package me.yuriisoft.buildnotify.mobile

import me.yuriisoft.buildnotify.mobile.domain.model.AppVersionProvider

/**
 * Reads the version name from the generated [BuildConfig].
 *
 * Requires `buildConfig = true` in the app-level `build.gradle.kts`.
 */
class AndroidAppVersionProvider : AppVersionProvider {
    override val versionName: String = BuildConfig.VERSION_NAME
}
