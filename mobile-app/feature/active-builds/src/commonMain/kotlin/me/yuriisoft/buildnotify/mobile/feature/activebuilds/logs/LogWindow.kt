package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

/**
 * What part of the log the screen is looking at: lines passing [filter], the newest
 * [limit] of them. Scrolling back raises [limit] one page at a time.
 */
data class LogWindow(
    val filter: LogFilter,
    val limit: Int,
) {
    fun older(): LogWindow = copy(limit = limit + PAGE_SIZE)

    fun withFilter(filter: LogFilter): LogWindow = LogWindow(filter, PAGE_SIZE)

    companion object {
        /** Lines read per page; one screen of the log is around 40. */
        const val PAGE_SIZE: Int = 200

        val Initial: LogWindow = LogWindow(LogFilter.ALL, PAGE_SIZE)
    }
}
