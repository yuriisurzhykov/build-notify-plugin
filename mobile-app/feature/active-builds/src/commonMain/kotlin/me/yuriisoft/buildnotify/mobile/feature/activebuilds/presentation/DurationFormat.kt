package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

/** `45s`, `1m 05s`, `1h 02m`. */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / TimeUnits.MILLIS_PER_SECOND
    val hours = totalSeconds / TimeUnits.SECONDS_PER_HOUR
    val minutes = (totalSeconds % TimeUnits.SECONDS_PER_HOUR) / TimeUnits.SECONDS_PER_MINUTE
    val seconds = totalSeconds % TimeUnits.SECONDS_PER_MINUTE
    return when {
        hours > 0   -> "${hours}h ${minutes.toString().padStart(2, '0')}m"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
        else        -> "${seconds}s"
    }
}
