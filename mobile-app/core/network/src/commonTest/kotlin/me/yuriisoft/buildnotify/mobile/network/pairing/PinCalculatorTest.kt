package me.yuriisoft.buildnotify.mobile.network.pairing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PinCalculatorTest {

    /**
     * Minimal SHA-256 is not available in commonTest without a library,
     * so we use a deterministic stub that returns a fixed 32-byte digest.
     * This lets us verify the modular-arithmetic and formatting logic
     * independently of the hash function.
     */
    private val stubHash = ByteArray(32) { it.toByte() }
    private val stubSha256: (ByteArray) -> ByteArray = { stubHash }

    private val calculator = PinCalculator(stubSha256)

    @Test
    fun pinIsAlwaysSixDigits() {
        val pin = calculator.derivePin("AA:BB", "CC:DD")
        assertEquals(6, pin.length)
    }

    @Test
    fun pinContainsOnlyDigits() {
        val pin = calculator.derivePin("AA:BB", "CC:DD")
        assertTrue(pin.all { it.isDigit() })
    }

    @Test
    fun pinIsDeterministic() {
        val pin1 = calculator.derivePin("AA:BB", "CC:DD")
        val pin2 = calculator.derivePin("AA:BB", "CC:DD")
        assertEquals(pin1, pin2)
    }

    @Test
    fun pinDependsOnFingerprintOrder() {
        val zeroHash = ByteArray(32)
        val calc = PinCalculator { input ->
            // Simple non-cryptographic hash that varies with input order:
            // XOR each input byte into a rotating position of a 32-byte array.
            val out = ByteArray(32)
            input.forEachIndexed { i, b -> out[i % 32] = (out[i % 32].toInt() xor b.toInt()).toByte() }
            out
        }
        val pinAB = calc.derivePin("SERVER_FP", "CLIENT_FP")
        val pinBA = calc.derivePin("CLIENT_FP", "SERVER_FP")
        // Swapping server/client must produce a different PIN (with overwhelming probability)
        assertTrue(pinAB != pinBA, "Swapping fingerprints should change the PIN")
    }

    @Test
    fun fingerprintsAreCaseInsensitive() {
        val inputs = mutableListOf<ByteArray>()
        val captureSha256: (ByteArray) -> ByteArray = { input ->
            inputs.add(input.copyOf())
            stubHash
        }
        val calc = PinCalculator(captureSha256)

        calc.derivePin("AA:BB:CC", "DD:EE:FF")
        calc.derivePin("aa:bb:cc", "dd:ee:ff")

        assertEquals(
            inputs[0].toList(),
            inputs[1].toList(),
            "Upper-case and lower-case fingerprints must produce the same hash input",
        )
    }

    @Test
    fun pinMatchesManualComputation() {
        // stubHash = [0x00, 0x01, 0x02, ..., 0x1F]
        // BigEndianUnsigned mod 1_000_000:
        //   iterative: acc = ((... * 256 + byte) % 1_000_000)
        var expected = 0L
        for (i in 0 until 32) {
            expected = (expected * 256 + i) % 1_000_000
        }
        val pin = calculator.derivePin("anything", "anything")
        assertEquals(expected.toString().padStart(6, '0'), pin)
    }

    @Test
    fun allZeroHashProducesZeroPaddedPin() {
        val calc = PinCalculator { ByteArray(32) }
        val pin = calc.derivePin("a", "b")
        assertEquals("000000", pin)
    }
}
