package me.yuriisoft.buildnotify.network.server

import me.yuriisoft.buildnotify.security.InMemoryTrustedClients
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrustedClientAuthorizerTest {

    private val store = InMemoryTrustedClients(setOf("AA:BB"))
    private val authorizer = TrustedClientAuthorizer { store }

    @Test
    fun `session without a client certificate is never authorized`() {
        assertFalse(authorizer.isAuthorized(null))
    }

    @Test
    fun `only trusted fingerprints are authorized`() {
        assertTrue(authorizer.isAuthorized("AA:BB"))
        assertFalse(authorizer.isAuthorized("CC:DD"))
    }

    @Test
    fun `revocation takes effect on the next check`() {
        store.revoke("AA:BB")
        assertFalse(authorizer.isAuthorized("AA:BB"))
    }
}
