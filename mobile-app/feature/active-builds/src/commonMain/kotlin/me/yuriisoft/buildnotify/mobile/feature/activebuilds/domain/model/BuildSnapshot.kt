package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

sealed interface BuildSnapshot {

    val buildId: String
    val projectName: String
    val startedAt: Long

    data class Active(
        override val buildId: String,
        override val projectName: String,
        override val startedAt: Long,
        val currentTask: String?,
    ) : BuildSnapshot

    data class Finished(
        override val buildId: String,
        override val projectName: String,
        override val startedAt: Long,
        val outcome: BuildOutcome,
    ) : BuildSnapshot
}
