package me.yuriisoft.buildnotify.mobile.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.yuriisoft.buildnotify.mobile.core.communication.StateCommunication

/**
 * Test double that records every state transition in [history].
 * The initial value is always the first entry in the history list.
 */
class RecordingStateCommunication<T : Any>(initial: T) : StateCommunication.Mutable<T> {

    private val flow = MutableStateFlow(initial)
    private val transitions = mutableListOf(initial)

    override val observe: StateFlow<T> = flow.asStateFlow()

    val history: List<T> get() = transitions.toList()

    override fun put(value: T) {
        transitions.add(value)
        flow.value = value
    }

    override fun update(transform: (T) -> T) {
        put(transform(flow.value))
    }
}
