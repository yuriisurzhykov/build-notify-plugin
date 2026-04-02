package me.yuriisoft.buildnotify.mobile.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun platformSha256(input: ByteArray): ByteArray {
    val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
    input.usePinned { inputPinned ->
        hash.usePinned { hashPinned ->
            CC_SHA256(inputPinned.addressOf(0), input.size.toUInt(), hashPinned.addressOf(0))
        }
    }
    return hash
}
