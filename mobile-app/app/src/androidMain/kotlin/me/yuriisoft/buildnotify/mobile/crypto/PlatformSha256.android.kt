package me.yuriisoft.buildnotify.mobile.crypto

import java.security.MessageDigest

actual fun platformSha256(input: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(input)