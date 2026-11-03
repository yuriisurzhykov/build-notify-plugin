package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildEvent

/** Write side of the build cache. The read side is a set of SQL-backed sources. */
interface BuildEventStore {

    /** Applies [events] in order, as one transaction: observers see the batch at once. */
    suspend fun write(events: List<BuildEvent>)

    /** Forgets builds still marked as running; their outcome can no longer arrive. */
    suspend fun dropActiveBuilds()
}
