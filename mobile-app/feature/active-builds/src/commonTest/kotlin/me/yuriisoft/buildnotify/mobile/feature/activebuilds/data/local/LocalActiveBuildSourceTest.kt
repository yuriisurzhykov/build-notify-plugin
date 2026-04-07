package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildSnapshotMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class LocalActiveBuildSourceTest {

    @Test
    fun observeUnit_emitsEmptyListInitially() = runTest {
        val source = localSource(createTestActiveBuildsDatabase())

        assertEquals(emptyList(), source.observe(Unit).first())
    }

    @Test
    fun save_persistsActiveBuildWithCurrentTaskAndNullDuration() = runTest {
        val source = localSource(createTestActiveBuildsDatabase())

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Active(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 42L,
                    currentTask = ":compile",
                ),
            ),
        )

        val list = source.observe(Unit).first()
        assertEquals(1, list.size)
        val active = assertIs<BuildSnapshot.Active>(list.single())
        assertEquals("b1", active.buildId)
        assertEquals("app", active.projectName)
        assertEquals(42L, active.startedAt)
        assertEquals(":compile", active.currentTask)
    }

    @Test
    fun save_persistsFinishedBuildWithDurationAndIssues() = runTest {
        val source = localSource(createTestActiveBuildsDatabase())

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Finished(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 10L,
                    outcome = BuildOutcome(
                        status = FinishStatus.FAILED,
                        durationMs = 999L,
                        errors = listOf(BuildIssue(message = "e1", filePath = "a.kt", line = 3)),
                        warnings = listOf(BuildIssue(message = "w1", filePath = null, line = null)),
                    ),
                ),
            ),
        )

        val finished = assertIs<BuildSnapshot.Finished>(source.observe(Unit).first().single())
        assertEquals(FinishStatus.FAILED, finished.outcome.status)
        assertEquals(999L, finished.outcome.durationMs)
        assertEquals(1, finished.outcome.errors.size)
        assertEquals("e1", finished.outcome.errors.single().message)
        assertEquals("a.kt", finished.outcome.errors.single().filePath)
        assertEquals(3, finished.outcome.errors.single().line)
        assertEquals(1, finished.outcome.warnings.size)
        assertEquals("w1", finished.outcome.warnings.single().message)
        assertNull(finished.outcome.warnings.single().filePath)
    }

    @Test
    fun save_upsertsSameBuildIdAndReplacesIssues() = runTest {
        val source = localSource(createTestActiveBuildsDatabase())

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Finished(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1L,
                    outcome = BuildOutcome(
                        status = FinishStatus.FAILED,
                        durationMs = 100L,
                        errors = listOf(BuildIssue("old", null, null)),
                        warnings = emptyList(),
                    ),
                ),
            ),
        )

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Finished(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1L,
                    outcome = BuildOutcome(
                        status = FinishStatus.SUCCESS,
                        durationMs = 200L,
                        errors = listOf(BuildIssue("new-error", "f.kt", 1)),
                        warnings = listOf(BuildIssue("new-warn", null, null)),
                    ),
                ),
            ),
        )

        val list = source.observe(Unit).first()
        assertEquals(1, list.size)
        val finished = assertIs<BuildSnapshot.Finished>(list.single())
        assertEquals(FinishStatus.SUCCESS, finished.outcome.status)
        assertEquals(200L, finished.outcome.durationMs)
        assertEquals(listOf("new-error"), finished.outcome.errors.map { it.message })
        assertEquals(listOf("new-warn"), finished.outcome.warnings.map { it.message })
    }

    @Test
    fun save_removesBuildsMissingFromIncomingList() = runTest {
        val source = localSource(createTestActiveBuildsDatabase())

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Active("b1", "a", 1L, null),
                BuildSnapshot.Active("b2", "b", 2L, null),
            ),
        )
        assertEquals(2, source.observe(Unit).first().size)

        source.save(Unit, listOf(BuildSnapshot.Active("b2", "b", 2L, null)))

        val remaining = source.observe(Unit).first()
        assertEquals(1, remaining.size)
        assertEquals("b2", remaining.single().buildId)
    }

    @Test
    fun deleteUnit_clearsBuildsAndCascadesIssueDeletion() = runTest {
        val db = createTestActiveBuildsDatabase()
        val source = localSource(db)

        source.save(
            Unit,
            listOf(
                BuildSnapshot.Finished(
                    buildId = "b1",
                    projectName = "app",
                    startedAt = 1L,
                    outcome = BuildOutcome(
                        status = FinishStatus.SUCCESS,
                        durationMs = 50L,
                        errors = emptyList(),
                        warnings = listOf(BuildIssue("w", null, null)),
                    ),
                ),
            ),
        )
        assertEquals(1, source.observe(Unit).first().size)

        source.delete(Unit)

        assertEquals(emptyList(), source.observe(Unit).first())
        assertEquals(0, db.buildIssueQueries.selectByBuildId("b1").executeAsList().size)
    }

    private fun TestScope.localSource(db: ActiveBuildsDatabase): LocalActiveBuildSource =
        LocalActiveBuildSource(
            activeBuildQueries = db.activeBuildQueries,
            issueQueries = db.buildIssueQueries,
            snapshotMapper = BuildSnapshotMapper(),
            dispatchers = FakeAppDispatchers(UnconfinedTestDispatcher(testScheduler)),
        )
}
