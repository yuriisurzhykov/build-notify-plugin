package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** `45s`, `1m 05s`, `1h 02m`. */
internal fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0   -> "${hours}h ${minutes.toString().padStart(2, '0')}m"
        minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
        else        -> "${seconds}s"
    }
}

@OptIn(ExperimentalTime::class)
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Milliseconds since [startedAt], re-evaluated once per second while in composition. */
@Composable
internal fun rememberElapsedMs(startedAt: Long): Long {
    val now by produceState(initialValue = currentTimeMillis(), startedAt) {
        while (true) {
            value = currentTimeMillis()
            delay(1_000)
        }
    }
    return (now - startedAt).coerceAtLeast(0)
}
