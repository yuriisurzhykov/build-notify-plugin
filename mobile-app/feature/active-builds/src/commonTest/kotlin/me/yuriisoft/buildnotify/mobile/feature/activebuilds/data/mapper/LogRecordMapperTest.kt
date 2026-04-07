package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildLogRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.LogRecordKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import kotlin.test.Test
import kotlin.test.assertEquals

class LogRecordMapperTest {

    private val mapper = LogRecordMapper()

    @Test
    fun taskKind_mapsToTaskLogKind() {
        val record = BuildLogRecord(
            buildId = "b1",
            timestamp = 1000L,
            message = ":app:compileKotlin",
            kind = LogRecordKind.TASK,
        )

        val result = mapper.map(record)

        assertEquals(LogKind.TASK, result.kind)
        assertEquals(1000L, result.timestamp)
        assertEquals(":app:compileKotlin", result.message)
    }

    @Test
    fun warningKind_mapsToWarningLogKind() {
        val record = BuildLogRecord(
            buildId = "b1",
            timestamp = 1100L,
            message = "deprecated API usage",
            kind = LogRecordKind.WARNING,
        )

        val result = mapper.map(record)

        assertEquals(LogKind.WARNING, result.kind)
        assertEquals(1100L, result.timestamp)
        assertEquals("deprecated API usage", result.message)
    }

    @Test
    fun errorKind_mapsToErrorLogKind() {
        val record = BuildLogRecord(
            buildId = "b1",
            timestamp = 1200L,
            message = "unresolved reference: foo",
            kind = LogRecordKind.ERROR,
        )

        val result = mapper.map(record)

        assertEquals(LogKind.ERROR, result.kind)
        assertEquals(1200L, result.timestamp)
        assertEquals("unresolved reference: foo", result.message)
    }
}
