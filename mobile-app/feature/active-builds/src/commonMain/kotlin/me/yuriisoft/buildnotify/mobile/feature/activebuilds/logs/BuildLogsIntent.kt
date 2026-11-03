package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BuildLogsIntent {

    @Immutable
    data class SelectFilter(val filter: LogFilter) : BuildLogsIntent

    /** The reader reached the oldest loaded line; load the page before it. */
    @Immutable
    data object LoadOlder : BuildLogsIntent
}
