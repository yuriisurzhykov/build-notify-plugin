package me.yuriisoft.buildnotify.security

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

class ClientToFuTrustManagerTest {

    private val cert = mockk<X509Certificate> {
        every { encoded } returns byteArrayOf(1, 2, 3)
        every { subjectX500Principal } returns X500Principal("CN=Pixel 9")
    }
    private val fingerprint = cert.sha256Fingerprint()
    private val prompts = mutableListOf<String>()

    private fun manager(store: TrustedClients) = ClientToFuTrustManager(
        store = store,
        serverFingerprint = "SERVER",
        onFirstSeen = { _, _, deviceName -> prompts += deviceName },
    )

    @Test
    fun `client without a certificate is rejected`() {
        assertThrows<CertificateException> {
            manager(InMemoryTrustedClients()).checkClientTrusted(emptyArray(), "RSA")
        }
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `trusted client passes`() {
        assertDoesNotThrow {
            manager(InMemoryTrustedClients(setOf(fingerprint))).checkClientTrusted(arrayOf(cert), "RSA")
        }
    }

    @Test
    fun `unknown client is held pending and triggers exactly one pairing prompt`() {
        val store = InMemoryTrustedClients()
        val manager = manager(store)

        assertThrows<CertificateException> { manager.checkClientTrusted(arrayOf(cert), "RSA") }
        assertThrows<CertificateException> { manager.checkClientTrusted(arrayOf(cert), "RSA") }

        assertTrue(store.isPending(fingerprint))
        assertEquals(listOf("Pixel 9"), prompts)
    }

    @Test
    fun `previously rejected client must be re-approved`() {
        val store = InMemoryTrustedClients().apply { reject(fingerprint) }

        assertThrows<CertificateException> { manager(store).checkClientTrusted(arrayOf(cert), "RSA") }

        assertTrue(store.isPending(fingerprint))
        assertEquals(listOf("Pixel 9"), prompts)
    }

    @Test
    fun `fingerprint is colon separated upper-case hex of the DER bytes`() {
        assertEquals(95, fingerprint.length)
        assertTrue(fingerprint.matches(Regex("([0-9A-F]{2}:){31}[0-9A-F]{2}")))
    }
}
