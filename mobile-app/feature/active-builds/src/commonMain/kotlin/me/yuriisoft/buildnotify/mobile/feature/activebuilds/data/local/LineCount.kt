package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

/** Lines of one kind a batch appended to one build, and when the first of them was logged. */
internal class LineCount(val firstTimestamp: Long) {
    var lines: Long = 0
}
