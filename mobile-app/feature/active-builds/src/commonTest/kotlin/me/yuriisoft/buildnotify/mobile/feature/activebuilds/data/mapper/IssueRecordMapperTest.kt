package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IssueRecordMapperTest {

    private val mapper = IssueRecordMapper()

    @Test
    fun mapsAllFields() {
        val record = BuildIssueRecord(
            buildId = "b1",
            message = "msg",
            filePath = "F.kt",
            line = 7,
            severity = IssueSeverity.ERROR,
        )

        val issue = mapper.map(record)

        assertEquals("msg", issue.message)
        assertEquals("F.kt", issue.filePath)
        assertEquals(7, issue.line)
    }

    @Test
    fun handlesNullFilePathAndLine() {
        val record = BuildIssueRecord(
            buildId = "b1",
            message = "project-level",
            filePath = null,
            line = null,
            severity = IssueSeverity.WARNING,
        )

        val issue = mapper.map(record)

        assertEquals("project-level", issue.message)
        assertNull(issue.filePath)
        assertNull(issue.line)
    }
}
