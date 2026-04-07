package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.IssueSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BuildSnapshotMapperTest {

    private val mapper = BuildSnapshotMapper()

    @Test
    fun activeRecord_withEmptyIssues_mapsToActiveSnapshot() {
        val record = BuildRecord(
            buildId = "b1",
            projectName = "app",
            status = BuildRecordStatus.ACTIVE,
            startedAt = 1000L,
            currentTask = ":compile",
            durationMs = null,
        )

        val snapshot = assertIs<BuildSnapshot.Active>(mapper.map(record to emptyList()))

        assertEquals("b1", snapshot.buildId)
        assertEquals("app", snapshot.projectName)
        assertEquals(1000L, snapshot.startedAt)
        assertEquals(":compile", snapshot.currentTask)
    }

    @Test
    fun successRecord_withIssues_mapsToFinishedWithOutcomeFromDurationAndSeveritySplit() {
        val record = BuildRecord(
            buildId = "b1",
            projectName = "app",
            status = BuildRecordStatus.SUCCESS,
            startedAt = 1000L,
            currentTask = null,
            durationMs = 5000L,
        )
        val issues = listOf(
            BuildIssueRecord(
                buildId = "b1",
                message = "warn",
                filePath = "W.kt",
                line = 1,
                severity = IssueSeverity.WARNING,
            ),
        )

        val snapshot = assertIs<BuildSnapshot.Finished>(mapper.map(record to issues))

        assertEquals(FinishStatus.SUCCESS, snapshot.outcome.status)
        assertEquals(5000L, snapshot.outcome.durationMs)
        assertTrue(snapshot.outcome.errors.isEmpty())
        assertEquals(1, snapshot.outcome.warnings.size)
        assertEquals("warn", snapshot.outcome.warnings[0].message)
        assertEquals("W.kt", snapshot.outcome.warnings[0].filePath)
    }

    @Test
    fun failedRecord_withMixedSeverities_splitsErrorsAndWarnings() {
        val record = BuildRecord(
            buildId = "b1",
            projectName = "app",
            status = BuildRecordStatus.FAILED,
            startedAt = 1000L,
            currentTask = null,
            durationMs = 3000L,
        )
        val issues = listOf(
            BuildIssueRecord("b1", "e1", "E.kt", 10, IssueSeverity.ERROR),
            BuildIssueRecord("b1", "w1", "W.kt", 2, IssueSeverity.WARNING),
            BuildIssueRecord("b1", "e2", null, null, IssueSeverity.ERROR),
        )

        val snapshot = assertIs<BuildSnapshot.Finished>(mapper.map(record to issues))

        assertEquals(FinishStatus.FAILED, snapshot.outcome.status)
        assertEquals(2, snapshot.outcome.errors.size)
        assertEquals(1, snapshot.outcome.warnings.size)
        assertEquals("e1", snapshot.outcome.errors[0].message)
        assertEquals("w1", snapshot.outcome.warnings[0].message)
    }

    @Test
    fun finishedRecord_withNoIssues_hasEmptyErrorsAndWarnings() {
        val record = BuildRecord(
            buildId = "b1",
            projectName = "app",
            status = BuildRecordStatus.CANCELLED,
            startedAt = 1000L,
            currentTask = null,
            durationMs = 100L,
        )

        val snapshot = assertIs<BuildSnapshot.Finished>(mapper.map(record to emptyList()))

        assertEquals(FinishStatus.CANCELLED, snapshot.outcome.status)
        assertTrue(snapshot.outcome.errors.isEmpty())
        assertTrue(snapshot.outcome.warnings.isEmpty())
    }
}
