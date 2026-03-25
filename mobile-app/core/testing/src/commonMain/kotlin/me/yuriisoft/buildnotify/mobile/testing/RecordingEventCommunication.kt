package me.yuriisoft.buildnotify.mobile.testing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import me.yuriisoft.buildnotify.mobile.core.communication.EventCommunication

/**
 * Test double that records every emitted event in [history].
 * Uses [Channel.UNLIMITED] so sends never suspend, making
 * assertions deterministic without `advanceUntilIdle`.
 */
class RecordingEventCommunication<T : Any> : EventCommunication.Mutable<T> {

    private val channel = Channel<T>(Channel.UNLIMITED)
    private val sent = mutableListOf<T>()

    override val observe: Flow<T> = channel.receiveAsFlow()

    val history: List<T> get() = sent.toList()

    override suspend fun send(event: T) {
        sent.add(event)
        channel.send(event)
    }

    override fun trySend(event: T): ChannelResult<Unit> {
        sent.add(event)
        return channel.trySend(event)
    }
}
