package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model

/** Size of a build's log, known without loading it. */
data class LogStats(
    val countsByKind: Map<LogKind, Int>,
    val firstTimestamp: Long?,
) {
    val total: Int get() = countsByKind.values.sum()

    fun count(kinds: Set<LogKind>): Int = kinds.sumOf { countsByKind[it] ?: 0 }

    companion object {
        val Empty: LogStats = LogStats(emptyMap(), null)
    }
}
