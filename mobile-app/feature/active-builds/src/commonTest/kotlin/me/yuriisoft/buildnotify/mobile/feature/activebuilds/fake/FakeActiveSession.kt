package me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.data.protocol.WsPayload
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession

class FakeActiveSession : ActiveSession {

    private val _incoming = MutableSharedFlow<WsPayload>()
    override val incoming: SharedFlow<WsPayload> = _incoming

    val sentEnvelopes: MutableList<WsEnvelope> = mutableListOf()

    override suspend fun send(envelope: WsEnvelope) {
        sentEnvelopes += envelope
    }

    suspend fun emit(payload: WsPayload) {
        _incoming.emit(payload)
    }
}
