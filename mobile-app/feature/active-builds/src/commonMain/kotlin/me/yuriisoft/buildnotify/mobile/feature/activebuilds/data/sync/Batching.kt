package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Groups a burst of values into lists: after the first value of a batch, waits [window]
 * for more to arrive, then emits up to [maxSize] of them at once.
 *
 * Values are never dropped. The buffer holds [maxSize] values; once it is full the
 * upstream collector suspends, which is the backpressure a hot event source needs.
 * Draining uses `tryReceive` only, so no value can be lost to a cancelled receive.
 */
internal fun <T> Flow<T>.batched(window: Duration, maxSize: Int): Flow<List<T>> {
    require(maxSize > 0) { "maxSize must be positive, was $maxSize" }
    return channelFlow {
        val buffer = Channel<T>(capacity = maxSize)
        launch {
            collect { buffer.send(it) }
            buffer.close()
        }
        while (true) {
            val first = buffer.receiveCatching().getOrNull() ?: break
            delay(window)
            val batch = ArrayList<T>(maxSize).apply { add(first) }
            while (batch.size < maxSize) {
                batch += buffer.tryReceive().getOrNull() ?: break
            }
            send(batch)
        }
    }
}
