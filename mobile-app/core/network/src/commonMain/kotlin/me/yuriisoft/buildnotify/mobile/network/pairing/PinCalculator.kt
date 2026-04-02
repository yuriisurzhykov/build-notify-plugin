package me.yuriisoft.buildnotify.mobile.network.pairing

/**
 * Derives a 6-digit numeric PIN from the combination of server and client
 * certificate fingerprints.
 *
 * Both the mobile app and the IDE plugin independently compute the same PIN
 * using `SHA-256(serverFP.lowercase() + clientFP.lowercase()) mod 1 000 000`,
 * allowing the user to visually verify that both devices see the same value
 * without transmitting the PIN over the wire.
 *
 * @param sha256 Platform-provided SHA-256 hash function. Accepts raw bytes
 *               and returns the 32-byte digest.
 */
class PinCalculator(private val sha256: (ByteArray) -> ByteArray) {

    /**
     * @param serverFingerprint SHA-256 fingerprint of the server certificate
     *                          (any hex format — colon-separated or raw).
     * @param clientFingerprint SHA-256 fingerprint of the client certificate.
     * @return A zero-padded 6-digit decimal string (e.g. `"042817"`).
     */
    fun derivePin(serverFingerprint: String, clientFingerprint: String): String {
        val combined = serverFingerprint.lowercase() + clientFingerprint.lowercase()
        val hash = sha256(combined.encodeToByteArray())
        val numeric = hash.toBigEndianUnsignedMod(PIN_MODULUS)
        return numeric.toString().padStart(PIN_LENGTH, '0')
    }

    companion object {
        private const val PIN_LENGTH = 6
        private const val PIN_MODULUS = 1_000_000L
    }
}

/**
 * Interprets this byte array as a big-endian unsigned integer and returns
 * `value mod [modulus]`.
 *
 * Equivalent to `java.math.BigInteger(1, this).mod(BigInteger.valueOf(modulus)).toLong()`
 * but avoids a JVM-only dependency. Safe from overflow because the intermediate
 * maximum `(modulus - 1) * 256 + 255` fits comfortably in a [Long].
 */
private fun ByteArray.toBigEndianUnsignedMod(modulus: Long): Long =
    fold(0L) { acc, byte ->
        (acc * 256 + (byte.toInt() and 0xFF)) % modulus
    }
