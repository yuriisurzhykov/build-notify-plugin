package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

data class TaskStartedEvent(
    val buildId: String,
    val taskPath: String,
    val timestamp: Long,
) : BuildEvent
