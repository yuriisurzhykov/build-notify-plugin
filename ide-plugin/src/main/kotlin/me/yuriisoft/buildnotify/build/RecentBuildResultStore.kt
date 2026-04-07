package me.yuriisoft.buildnotify.build

import com.intellij.openapi.components.Service
import me.yuriisoft.buildnotify.build.model.BuildResult
import java.util.ArrayDeque

/**
 * Application-level store that retains the [capacity] most recently completed
 * [BuildResult]s so that newly connecting clients can receive them as part of
 * [BuildSnapshotPayload.recentResults].
 *
 * Results are stored in insertion order; the oldest entry is evicted when the
 * store exceeds [capacity]. All public methods are `@Synchronized` — the store
 * is written from Gradle listener threads and read from the WebSocket dispatch
 * thread, so simple method-level synchronization is sufficient.
 *
 * ### Contract
 * - [record] must be called once per completed build, **after** broadcasting
 *   the `BuildResultPayload` to connected clients (those clients don't need the
 *   result via snapshot; only future clients do).
 * - [recentResults] returns a stable snapshot of the current contents as an
 *   immutable list; callers may iterate it freely without holding any lock.
 */
@Service(Service.Level.APP)
class RecentBuildResultStore(private val capacity: Int = DEFAULT_CAPACITY) {

    private val results = ArrayDeque<BuildResult>(capacity + 1)

    /**
     * Records a completed build result, evicting the oldest entry if
     * the store has reached [capacity].
     */
    @Synchronized
    fun record(result: BuildResult) {
        results.addLast(result)
        if (results.size > capacity) results.removeFirst()
    }

    /**
     * Returns an immutable snapshot of all retained results, oldest first.
     */
    @Synchronized
    fun recentResults(): List<BuildResult> = results.toList()

    companion object {
        private const val DEFAULT_CAPACITY = 10
    }
}
