package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

/**
 * The IDE's view right after connecting: what is running now and the latest results.
 * Authoritative for running builds only; finished history on the device is kept.
 */
data class SnapshotEvent(
    val activeBuilds: List<SnapshotBuild>,
    val recentResults: List<BuildResultEvent>,
) : BuildEvent
