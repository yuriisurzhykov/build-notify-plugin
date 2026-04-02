package me.yuriisoft.buildnotify.mobile.crypto

/**
 * Platform-provided SHA-256 hash function.
 *
 * Used by [PinCalculator] to derive the 6-digit pairing PIN from
 * certificate fingerprints. Each platform source set provides the
 * actual implementation:
 * - Android: `java.security.MessageDigest`
 * - iOS: `CommonCrypto/CC_SHA256`
 */
expect fun platformSha256(input: ByteArray): ByteArray
