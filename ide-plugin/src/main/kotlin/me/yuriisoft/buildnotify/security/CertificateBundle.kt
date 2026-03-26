package me.yuriisoft.buildnotify.security

import javax.net.ssl.SSLContext

data class CertificateBundle(
    val sslContext: SSLContext,
    val fingerprint: String,
)