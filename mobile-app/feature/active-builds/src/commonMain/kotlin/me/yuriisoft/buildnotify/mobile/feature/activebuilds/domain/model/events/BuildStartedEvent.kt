package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

data class BuildStartedEvent(
    val buildId: String,
    val projectName: String,
    val startedAt: Long,
) : BuildEvent
