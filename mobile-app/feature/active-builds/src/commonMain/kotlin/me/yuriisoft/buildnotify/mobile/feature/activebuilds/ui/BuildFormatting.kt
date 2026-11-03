package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_hours
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_minutes
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_now
import org.jetbrains.compose.resources.stringResource
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

/** `m:ss` for the live timer, `h:mm:ss` past an hour. */
internal fun formatTimer(elapsedMs: Long): String {
    val total = elapsedMs.coerceAtLeast(0) / 1_000
    val h = total / 3_600
    val m = (total % 3_600) / 60
    val s = (total % 60).toString().padStart(2, '0')
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$s" else "$m:$s"
}

/** "сейчас", "5 мин назад", "2 ч назад" — re-evaluated every second while shown. */
@Composable
internal fun relativeTime(atMillis: Long): String {
    val minutes = rememberElapsedMs(atMillis) / 60_000
    return when {
        minutes < 1  -> stringResource(Res.string.time_now)
        minutes < 60 -> stringResource(Res.string.time_minutes, minutes.toInt())
        else         -> stringResource(Res.string.time_hours, (minutes / 60).toInt())
    }
}
