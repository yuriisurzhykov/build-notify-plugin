package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_hours
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_minutes
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.generated.resources.time_now
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation.TimeUnits
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Milliseconds since [startedAt], re-evaluated once per second while in composition. */
@Composable
internal fun rememberElapsedMs(startedAt: Long): Long {
    val now by produceState(initialValue = currentTimeMillis(), startedAt) {
        while (true) {
            value = currentTimeMillis()
            delay(TimeUnits.MILLIS_PER_SECOND.toLong())
        }
    }
    return (now - startedAt).coerceAtLeast(0)
}

/** `m:ss` for the live timer, `h:mm:ss` past an hour. */
internal fun formatTimer(elapsedMs: Long): String {
    val total = elapsedMs.coerceAtLeast(0) / TimeUnits.MILLIS_PER_SECOND
    val h = total / TimeUnits.SECONDS_PER_HOUR
    val m = (total % TimeUnits.SECONDS_PER_HOUR) / TimeUnits.SECONDS_PER_MINUTE
    val s = (total % TimeUnits.SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$s" else "$m:$s"
}

/** "сейчас", "5 мин назад", "2 ч назад" — re-evaluated every second while shown. */
@Composable
internal fun relativeTime(atMillis: Long): String {
    val minutes = rememberElapsedMs(atMillis) / TimeUnits.MILLIS_PER_MINUTE
    return when {
        minutes < 1                          -> stringResource(Res.string.time_now)
        minutes < TimeUnits.MINUTES_PER_HOUR -> stringResource(Res.string.time_minutes, minutes.toInt())
        else                                 ->
            stringResource(Res.string.time_hours, (minutes / TimeUnits.MINUTES_PER_HOUR).toInt())
    }
}
