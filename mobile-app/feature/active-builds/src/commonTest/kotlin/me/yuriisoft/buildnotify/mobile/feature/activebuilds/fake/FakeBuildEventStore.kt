package me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.BuildEventStore
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent

class FakeBuildEventStore : BuildEventStore {

    val batches: MutableList<List<BuildEvent>> = mutableListOf()
    var dropCount: Int = 0
        private set

    /** When set, the next [write] throws it once. */
    var failNextWrite: Throwable? = null

    override suspend fun write(events: List<BuildEvent>) {
        val failure = failNextWrite
        if (failure != null) {
            failNextWrite = null
            throw failure
        }
        batches += events
    }

    override suspend fun dropActiveBuilds() {
        dropCount++
    }
}
