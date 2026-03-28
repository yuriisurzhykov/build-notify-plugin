package me.yuriisoft.buildnotify.security

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service

/**
 * [TrustedClients] implementation backed by [PropertiesComponent].
 *
 * [PropertiesComponent] is an IDE-global, application-scoped key-value store that
 * survives IDE restarts automatically — no manual serialization needed.
 *
 * Storage layout (all keys are private constants):
 *   "buildnotify.trusted_clients.trusted"  → comma-separated trusted fingerprints
 *   "buildnotify.trusted_clients.rejected" → comma-separated rejected fingerprints
 *
 * Pending state is **in-memory only**: it does not survive IDE restarts, which is
 * intentional — a restart clears the approval queue, forcing the dialog to reappear
 * on the next connection attempt. This avoids stale pending entries accumulating.
 *
 * Thread-safety: all mutations are `@Synchronized`. Reads use the same lock because
 * [PropertiesComponent.getValue] / [setValue] are not documented as thread-safe,
 * and [checkClientTrusted] arrives on the java-websocket I/O thread pool.
 *
 * Registered as an application-level service in `plugin.xml`:
 * ```xml
 * <applicationService serviceImplementation="...security.PersistentTrustedClients"/>
 * ```
 */
@Service(Service.Level.APP)
class PersistentTrustedClients : TrustedClients {

    // In-memory pending set — survives only for the duration of this IDE process.
    private val pendingSet = mutableSetOf<String>()

    @Synchronized
    override fun isTrusted(clientFingerprint: String): Boolean =
        loadSet(KEY_TRUSTED).contains(clientFingerprint)

    @Synchronized
    override fun isPending(clientFingerprint: String): Boolean =
        pendingSet.contains(clientFingerprint)

    @Synchronized
    override fun markPending(clientFingerprint: String) {
        if (isTrustedUnsafe(clientFingerprint)) return   // already approved — no-op
        pendingSet.add(clientFingerprint)
    }

    @Synchronized
    override fun trust(clientFingerprint: String) {
        pendingSet.remove(clientFingerprint)
        saveSet(KEY_REJECTED, loadSet(KEY_REJECTED) - clientFingerprint)
        saveSet(KEY_TRUSTED, loadSet(KEY_TRUSTED) + clientFingerprint)
    }

    @Synchronized
    override fun reject(clientFingerprint: String) {
        pendingSet.remove(clientFingerprint)
        saveSet(KEY_TRUSTED, loadSet(KEY_TRUSTED) - clientFingerprint)
        saveSet(KEY_REJECTED, loadSet(KEY_REJECTED) + clientFingerprint)
    }

    @Synchronized
    override fun revoke(clientFingerprint: String) {
        pendingSet.remove(clientFingerprint)
        saveSet(KEY_TRUSTED, loadSet(KEY_TRUSTED) - clientFingerprint)
        saveSet(KEY_REJECTED, loadSet(KEY_REJECTED) - clientFingerprint)
    }

    @Synchronized
    override fun trustedFingerprints(): Set<String> = loadSet(KEY_TRUSTED).toSet()

    /**
     * Returns `true` if [clientFingerprint] is in the rejected set.
     * Used internally by [ClientToFuTrustManager] to distinguish
     * "pending" from "explicitly rejected".
     */
    @Synchronized
    fun isRejected(clientFingerprint: String): Boolean =
        loadSet(KEY_REJECTED).contains(clientFingerprint)

    /** Must be called from a `@Synchronized` context. */
    private fun isTrustedUnsafe(clientFingerprint: String): Boolean =
        loadSet(KEY_TRUSTED).contains(clientFingerprint)

    private fun loadSet(key: String): Set<String> {
        val raw = PropertiesComponent.getInstance().getValue(key, "")
        if (raw.isBlank()) return emptySet()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }.toSet()
    }

    private fun saveSet(key: String, set: Set<String>) {
        PropertiesComponent.getInstance().setValue(key, set.joinToString(SEPARATOR))
    }

    private companion object {
        const val KEY_TRUSTED = "buildnotify.trusted_clients.trusted"
        const val KEY_REJECTED = "buildnotify.trusted_clients.rejected"
        const val SEPARATOR = ","
    }
}