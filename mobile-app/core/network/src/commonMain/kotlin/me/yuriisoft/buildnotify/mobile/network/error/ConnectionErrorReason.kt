package me.yuriisoft.buildnotify.mobile.network.error

/**
 * Classifies why a WebSocket connection failed or was lost.
 *
 * Each subtype carries context specific to the failure mode, enabling the
 * UI to display targeted troubleshooting hints without resorting to string
 * parsing. New failure categories are added by creating a new subtype —
 * existing code remains untouched (OCP).
 *
 * Produced by [ErrorRecognizer] implementations and composed via the
 * `ErrorMapping` mapper.
 *
 * [ClientRejected] — fired when the IDE plugin's `ClientToFuTrustManager`
 * throws `CertificateException("Client explicitly rejected")`. This is a
 * permanent (non-retriable) failure: the user must manually revoke their
 * decision in plugin settings and re-pair.
 *
 * The discriminant in [HandshakeErrors] matches on this exact exception message
 * produced by `PersistentTrustedClients.reject()` → `ClientToFuTrustManager`.
 * If the message text ever changes on the server side, [HandshakeErrors] must
 * be updated in tandem.
 */
sealed interface ConnectionErrorReason {
    val message: String

    data class Timeout(override val message: String) : ConnectionErrorReason
    data class Refused(override val message: String) : ConnectionErrorReason
    data class HandshakeFailed(override val message: String) : ConnectionErrorReason
    data class Lost(override val message: String) : ConnectionErrorReason

    /**
     * The IDE plugin explicitly rejected this device's client certificate.
     *
     * This means the user clicked "Reject" in the `ClientApprovalDialog` on
     * the plugin side. The connection will not succeed until:
     *   1. The user removes the rejection in plugin Settings → Trusted Clients.
     *   2. The mobile client disconnects and attempts a fresh connection (which
     *      will re-trigger the IDE approval dialog).
     *
     * **Retry behaviour:** [BudgetedReconnection.shouldRetry] returns `false` for
     * this error class — retrying is pointless because the server will keep
     * throwing `CertificateException("Client explicitly rejected")` until the
     * rejection is manually cleared. The [ConnectionOrchestrator]'s `retryWhen`
     * therefore stops retrying and transitions to [ConnectionState.Failed].
     */
    data class ClientRejected(override val message: String) : ConnectionErrorReason

    /**
     * The PIN-based pairing flow was not completed within the allowed time.
     *
     * This happens when either the mobile user or the IDE user fails to
     * confirm/reject the PIN before the [ConnectionOrchestrator]'s pairing
     * timeout expires. The mobile should return to the discovery screen so
     * the user can retry the connection (which will re-trigger pairing).
     */
    data class PairingTimeout(override val message: String) : ConnectionErrorReason

    /**
     * The mobile user explicitly rejected the PIN during the pairing flow.
     *
     * Unlike [ClientRejected] (which originates from the IDE side), this
     * reason is produced locally when the user taps "Cancel" on the pairing
     * confirmation screen. The connection is not retried automatically —
     * the user must initiate a new scan/connection attempt.
     */
    data class PairingRejected(override val message: String) : ConnectionErrorReason

    data class Unknown(override val message: String) : ConnectionErrorReason
}
