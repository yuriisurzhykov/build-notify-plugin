package me.yuriisoft.buildnotify.security


/**
 * Strategy: knows how to produce (or load) a TLS certificate bundle.
 * Returns null when this provider is not applicable or fails gracefully.
 */
interface SslContextProvider {

    /**
     * Whether this provider has enough configuration to attempt loading.
     * Checked before [provide] to skip unnecessary work.
     */
    fun isApplicable(): Boolean

    /**
     * Attempts to load or generate a [CertificateBundle].
     * Must not throw — returns null on any unrecoverable failure.
     */
    fun provide(): CertificateBundle?
}