package me.yuriisoft.buildnotify.security

import java.security.MessageDigest
import java.security.cert.X509Certificate

/**
 * SHA-256 of the certificate's DER encoding as colon-separated upper-case hex
 * (`AB:CD:EF:…`). The single definition of a client identity on the server: the TLS
 * trust manager and the session guard must compute it identically, and it matches
 * the mobile `ClientCertificateManager.fingerprint()` format.
 */
internal fun X509Certificate.sha256Fingerprint(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(encoded)
        .joinToString(":") { byte -> "%02X".format(byte) }
