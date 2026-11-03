package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import kotlin.test.Test
import kotlin.test.assertEquals

class LogWindowTest {

    @Test
    fun older_growsByOnePage() {
        assertEquals(LogWindow.PAGE_SIZE * 2, LogWindow.Initial.older().limit)
    }

    @Test
    fun changingFilter_resetsToOnePage() {
        val scrolled = LogWindow.Initial.older().older()
        assertEquals(LogWindow(LogFilter.ERRORS, LogWindow.PAGE_SIZE), scrolled.withFilter(LogFilter.ERRORS))
    }
}
