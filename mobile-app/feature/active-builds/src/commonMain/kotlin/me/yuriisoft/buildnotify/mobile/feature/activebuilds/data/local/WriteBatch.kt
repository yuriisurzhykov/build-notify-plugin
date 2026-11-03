package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind

/**
 * What one [SqlBuildEventStore.write] has learned so far, so that per-line work is done
 * once per batch: the current task is one UPDATE per build, and line counters are one
 * UPDATE per build and kind, however many lines the batch appends.
 */
internal class WriteBatch(knownBuilds: Collection<String>) {

    private val known: MutableSet<String> = knownBuilds.toHashSet()
    private val currentTasks = LinkedHashMap<String, String>()
    private val lineCounts = LinkedHashMap<Pair<String, LogRecordKind>, LineCount>()

    fun isKnown(buildId: String): Boolean = buildId in known

    fun added(buildId: String) {
        known += buildId
    }

    fun removed(buildId: String) {
        known -= buildId
        currentTasks -= buildId
        lineCounts.keys.removeAll { it.first == buildId }
    }

    fun taskStarted(buildId: String, taskPath: String) {
        currentTasks[buildId] = taskPath
    }

    fun finished(buildId: String) {
        currentTasks -= buildId
    }

    fun lineAppended(buildId: String, kind: LogRecordKind, timestamp: Long) {
        val count = lineCounts.getOrPut(buildId to kind) { LineCount(firstTimestamp = timestamp) }
        count.lines++
    }

    fun forEachCurrentTask(action: (buildId: String, taskPath: String) -> Unit) {
        currentTasks.forEach { (buildId, task) -> action(buildId, task) }
    }

    fun forEachLineCount(action: (buildId: String, kind: LogRecordKind, count: LineCount) -> Unit) {
        lineCounts.forEach { (key, count) -> action(key.first, key.second, count) }
    }
}
