package me.yuriisoft.buildnotify.mobile.network.pairing

import me.yuriisoft.buildnotify.mobile.network.connection.DiscoveredHost
import me.yuriisoft.buildnotify.mobile.network.tls.ClientIdentityProvider
import me.yuriisoft.buildnotify.mobile.network.tls.TrustedServers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PairingCoordinatorTest {

    private val stubSha256: (ByteArray) -> ByteArray = { ByteArray(32) { it.toByte() } }
    private val pinCalculator = PinCalculator(stubSha256)
    private val trustedServers = InMemoryTrustedServers()

    private val secureHost = DiscoveredHost(
        name = "My IDE",
        host = "10.0.0.1",
        port = 8765,
        scheme = "wss",
        fingerprint = "AA:BB:CC",
        instanceId = "test-instance-id",
    )

    private val plainHost = DiscoveredHost(
        name = "Plain IDE",
        host = "10.0.0.2",
        port = 8765,
        scheme = "ws",
    )

    private val serverFp = "AB:CD:EF:01:23:45:67:89"
    private val clientFp = "98:76:54:32:10:FE:DC:BA"
    private val fakeClientIdentity = FakeClientIdentityProvider(clientFp)

    private fun createCoordinator() =
        PairingCoordinator(pinCalculator, trustedServers, fakeClientIdentity)

    // region Initial state

    @Test
    fun initialStateIsIdle() {
        val coordinator = createCoordinator()

        assertIs<PairingState.Idle>(coordinator.state.value)
    }

    // endregion

    // region isPairingRequired

    @Test
    fun pairingRequiredForSecureUnpinnedHost() {
        val coordinator = createCoordinator()

        assertTrue(coordinator.isPairingRequired(secureHost))
    }

    @Test
    fun pairingNotRequiredForPlainHost() {
        val coordinator = createCoordinator()

        assertFalse(coordinator.isPairingRequired(plainHost))
    }

    @Test
    fun pairingNotRequiredWhenAlreadyPinned() {
        trustedServers.pin(secureHost.trustKey, "some-fingerprint")
        val coordinator = createCoordinator()

        assertFalse(coordinator.isPairingRequired(secureHost))
    }

    // endregion

    // region startPairing

    @Test
    fun startPairingTransitionsToAwaitingConfirmation() {
        val coordinator = createCoordinator()

        coordinator.startPairing(secureHost, serverFp)

        assertIs<PairingState.AwaitingConfirmation>(coordinator.state.value)
    }

    @Test
    fun startPairingComputesPin() {
        val coordinator = createCoordinator()
        val expectedPin = pinCalculator.derivePin(serverFp, clientFp)

        coordinator.startPairing(secureHost, serverFp)

        val state = coordinator.state.value as PairingState.AwaitingConfirmation
        assertEquals(expectedPin, state.pin)
    }

    @Test
    fun startPairingCarriesServerName() {
        val coordinator = createCoordinator()

        coordinator.startPairing(secureHost, serverFp)

        val state = coordinator.state.value as PairingState.AwaitingConfirmation
        assertEquals(secureHost.name, state.serverName)
    }

    @Test
    fun startPairingCarriesHostAndFingerprint() {
        val coordinator = createCoordinator()

        coordinator.startPairing(secureHost, serverFp)

        val state = coordinator.state.value as PairingState.AwaitingConfirmation
        assertEquals(secureHost, state.host)
        assertEquals(serverFp, state.serverFingerprint)
    }

    // endregion

    // region confirm

    @Test
    fun confirmPinsServerAndTransitionsToConfirmed() {
        val coordinator = createCoordinator()
        coordinator.startPairing(secureHost, serverFp)

        coordinator.confirm()

        assertIs<PairingState.Confirmed>(coordinator.state.value)
        assertEquals(serverFp, trustedServers.fingerprint(secureHost.trustKey))
    }

    @Test
    fun confirmIsNoOpWhenIdle() {
        val coordinator = createCoordinator()

        coordinator.confirm()

        assertIs<PairingState.Idle>(coordinator.state.value)
        assertFalse(trustedServers.isPinned(secureHost.trustKey))
    }

    @Test
    fun confirmIsNoOpWhenAlreadyConfirmed() {
        val coordinator = createCoordinator()
        coordinator.startPairing(secureHost, serverFp)
        coordinator.confirm()

        coordinator.confirm()

        assertIs<PairingState.Confirmed>(coordinator.state.value)
    }

    // endregion

    // region reject

    @Test
    fun rejectTransitionsToRejectedWithoutPinning() {
        val coordinator = createCoordinator()
        coordinator.startPairing(secureHost, serverFp)

        coordinator.reject()

        assertIs<PairingState.Rejected>(coordinator.state.value)
        assertFalse(trustedServers.isPinned(secureHost.trustKey))
    }

    @Test
    fun rejectIsNoOpWhenIdle() {
        val coordinator = createCoordinator()

        coordinator.reject()

        assertIs<PairingState.Idle>(coordinator.state.value)
    }

    // endregion

    // region reset

    @Test
    fun resetReturnsToIdle() {
        val coordinator = createCoordinator()
        coordinator.startPairing(secureHost, serverFp)
        coordinator.confirm()

        coordinator.reset()

        assertIs<PairingState.Idle>(coordinator.state.value)
    }

    @Test
    fun resetAfterRejectReturnsToIdle() {
        val coordinator = createCoordinator()
        coordinator.startPairing(secureHost, serverFp)
        coordinator.reject()

        coordinator.reset()

        assertIs<PairingState.Idle>(coordinator.state.value)
    }

    // endregion

    // region Full flow

    @Test
    fun fullPairingFlowPinsServerCorrectly() {
        val coordinator = createCoordinator()

        assertIs<PairingState.Idle>(coordinator.state.value)
        assertTrue(coordinator.isPairingRequired(secureHost))

        coordinator.startPairing(secureHost, serverFp)
        val awaiting = coordinator.state.value as PairingState.AwaitingConfirmation
        assertEquals(6, awaiting.pin.length)
        assertTrue(awaiting.pin.all { it.isDigit() })

        coordinator.confirm()
        assertIs<PairingState.Confirmed>(coordinator.state.value)
        assertTrue(trustedServers.matches(secureHost.trustKey, serverFp))
        assertFalse(coordinator.isPairingRequired(secureHost))

        coordinator.reset()
        assertIs<PairingState.Idle>(coordinator.state.value)
    }

    @Test
    fun canRepairAfterRejection() {
        val coordinator = createCoordinator()

        coordinator.startPairing(secureHost, serverFp)
        coordinator.reject()
        coordinator.reset()

        coordinator.startPairing(secureHost, serverFp)
        assertIs<PairingState.AwaitingConfirmation>(coordinator.state.value)
    }

    // endregion
}

/**
 * Minimal in-memory [TrustedServers] for unit tests.
 */
private class InMemoryTrustedServers : TrustedServers {
    private val store = mutableMapOf<String, String>()

    override fun fingerprint(instanceId: String): String? = store[instanceId]
    override fun pin(instanceId: String, fingerprint: String) { store[instanceId] = fingerprint }
    override fun unpin(instanceId: String) { store.remove(instanceId) }
}

private class FakeClientIdentityProvider(
    private val fp: String,
) : ClientIdentityProvider {
    override fun fingerprint(): String = fp
}
