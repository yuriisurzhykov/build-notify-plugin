package me.yuriisoft.buildnotify.mobile.tls

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import me.yuriisoft.buildnotify.mobile.network.tls.TrustedServers

/**
 * Android [TrustedServers] backed by an app-private [SharedPreferences] file.
 *
 * Each entry maps an `instanceId` to its pinned SHA-256 fingerprint.
 */
class SharedPrefsTrustedServers(context: Context) : TrustedServers {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun fingerprint(instanceId: String): String? =
        prefs.getString(instanceId, null)

    override fun pin(instanceId: String, fingerprint: String) {
        prefs.edit { putString(instanceId, fingerprint) }
    }

    override fun unpin(instanceId: String) {
        prefs.edit { remove(instanceId) }
    }

    private companion object {
        const val PREFS_NAME = "buildnotify_trusted_servers"
    }
}
