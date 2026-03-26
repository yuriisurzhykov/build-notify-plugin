package me.yuriisoft.buildnotify.mobile.tls

import me.yuriisoft.buildnotify.mobile.network.tls.TrustedServers
import platform.Foundation.NSUserDefaults

/**
 * iOS [TrustedServers] backed by [NSUserDefaults].
 *
 * Keys are prefixed with [KEY_PREFIX] to avoid collisions with other
 * user defaults entries.
 */
class UserDefaultsTrustedServers : TrustedServers {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun fingerprint(instanceId: String): String? =
        defaults.stringForKey(prefixed(instanceId))

    override fun pin(instanceId: String, fingerprint: String) {
        defaults.setObject(fingerprint, forKey = prefixed(instanceId))
    }

    override fun unpin(instanceId: String) {
        defaults.removeObjectForKey(prefixed(instanceId))
    }

    private fun prefixed(key: String): String = "$KEY_PREFIX$key"

    private companion object {
        const val KEY_PREFIX = "buildnotify.trusted."
    }
}
