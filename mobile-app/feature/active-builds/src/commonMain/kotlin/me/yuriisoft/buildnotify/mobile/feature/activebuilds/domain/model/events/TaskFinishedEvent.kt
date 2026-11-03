package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events

data class TaskFinishedEvent(
    val buildId: String,
    val taskPath: String,
    val status: String,
    val timestamp: Long,
) : BuildEvent
