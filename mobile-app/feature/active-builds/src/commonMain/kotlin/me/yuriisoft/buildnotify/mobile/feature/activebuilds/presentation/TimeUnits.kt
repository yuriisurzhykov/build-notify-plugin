package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

/** Unit conversions shared by every duration and timestamp formatter. */
internal object TimeUnits {
    const val MILLIS_PER_SECOND = 1_000
    const val MILLIS_PER_TENTH = 100
    const val SECONDS_PER_MINUTE = 60
    const val MINUTES_PER_HOUR = 60
    const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
    const val MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
}
