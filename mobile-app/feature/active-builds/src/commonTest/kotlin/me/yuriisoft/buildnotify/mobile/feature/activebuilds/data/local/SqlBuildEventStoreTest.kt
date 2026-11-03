package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildSnapshotMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.LogRecordMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogQuery
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildResultEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.DiagnosticSeverity
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotBuild
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.SnapshotEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskFinishedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlBuildEventStoreTest {

    private val allKinds = LogKind.entries.toSet()

    @Test
    fun startedBuild_isActiveWithTheLatestTaskOfTheBatch() = runTest {
        val env = env()
        env.store.write(
            listOf(
                started("b1"),
                TaskStartedEvent("b1", ":a", 10),
                TaskStartedEvent("b1", ":b", 11),
            ),
        )

        val build = assertIs<BuildSnapshot.Active>(env.builds().single())
        assertEquals(":b", build.currentTask)
    }

    @Test
    fun taskAndDiagnosticEvents_appendLinesInOrder() = runTest {
        val env = env()
        env.store.write(
            listOf(
                started("b1"),
                TaskStartedEvent("b1", ":compile", 10),
                DiagnosticEvent("b1", DiagnosticSeverity.WARNING, "deprecated", null, null, 11),
                TaskFinishedEvent("b1", ":compile", "SUCCESS", 12),
            ),
        )

        val lines = env.logs(LogQuery("b1", allKinds, limit = 10))
        assertEquals(listOf(":compile", "deprecated", ":compile SUCCESS"), lines.map { it.message })
        assertEquals(listOf(LogKind.TASK, LogKind.WARNING, LogKind.TASK), lines.map { it.kind })
        assertTrue(lines.zipWithNext().all { (a, b) -> a.id < b.id }, "ids grow with the log")
    }

    @Test
    fun linesOfUnknownBuilds_areIgnored() = runTest {
        val env = env()
        env.store.write(listOf(TaskStartedEvent("ghost", ":a", 1)))

        assertEquals(0L, env.db.buildLogQueries.logCount("ghost").executeAsOne())
    }

    @Test
    fun window_returnsOnlyTheNewestLinesOfTheRequestedKinds() = runTest {
        val env = env()
        val events = listOf(started("b1")) + (1..500).map { i ->
            if (i % 100 == 0) {
                DiagnosticEvent("b1", DiagnosticSeverity.ERROR, "error $i", null, null, i.toLong())
            } else {
                TaskStartedEvent("b1", ":task$i", i.toLong())
            }
        }
        env.store.write(events)

        val tail = env.logs(LogQuery("b1", allKinds, limit = 3))
        val errors = env.logs(LogQuery("b1", setOf(LogKind.ERROR), limit = 2))

        assertEquals(listOf(":task498", ":task499", "error 500"), tail.map { it.message })
        assertEquals(listOf("error 400", "error 500"), errors.map { it.message })
    }

    @Test
    fun stats_countLinesPerKindWithoutLoadingThem() = runTest {
        val env = env()
        env.store.write(
            listOf(
                started("b1"),
                TaskStartedEvent("b1", ":a", 5),
                DiagnosticEvent("b1", DiagnosticSeverity.ERROR, "boom", null, null, 7),
                DiagnosticEvent("b1", DiagnosticSeverity.ERROR, "boom 2", null, null, 8),
            ),
        )

        val stats = env.stats("b1")
        assertEquals(mapOf(LogKind.TASK to 1, LogKind.ERROR to 2), stats.countsByKind)
        assertEquals(3, stats.total)
        assertEquals(5L, stats.firstTimestamp)
    }

    @Test
    fun counters_accumulateAcrossBatches() = runTest {
        val env = env()
        env.store.write(listOf(started("b1"), TaskStartedEvent("b1", ":a", 3)))
        env.store.write(listOf(TaskStartedEvent("b1", ":b", 4), TaskFinishedEvent("b1", ":b", "SUCCESS", 5)))

        val stats = env.stats("b1")
        assertEquals(mapOf(LogKind.TASK to 3), stats.countsByKind)
        assertEquals(3L, stats.firstTimestamp)
        assertEquals(3L, env.db.buildLogQueries.logCount("b1").executeAsOne())
    }

    @Test
    fun buildDroppedLaterInTheSameBatch_leavesNoCountersBehind() = runTest {
        val env = env()
        env.store.write(listOf(started("stale")))
        env.store.write(
            listOf(
                TaskStartedEvent("stale", ":a", 1),
                SnapshotEvent(activeBuilds = emptyList(), recentResults = emptyList()),
            ),
        )

        assertEquals(emptyList(), env.builds())
        assertEquals(0, env.stats("stale").total)
    }

    @Test
    fun result_finishesTheBuild_keepsItsLog_andStoresIssues() = runTest {
        val env = env()
        env.store.write(listOf(started("b1"), TaskStartedEvent("b1", ":a", 1)))
        env.store.write(listOf(result("b1", FinishStatus.FAILED, errors = listOf(BuildIssue("boom", "A.kt", 3)))))

        val build = assertIs<BuildSnapshot.Finished>(env.builds().single())
        assertEquals(FinishStatus.FAILED, build.outcome.status)
        assertEquals(listOf("boom"), build.outcome.errors.map { it.message })
        assertEquals(1, env.stats("b1").total)
    }

    @Test
    fun repeatedResult_doesNotDuplicateIssues() = runTest {
        val env = env()
        val result = result("b1", FinishStatus.FAILED, errors = listOf(BuildIssue("boom", null, null)))
        env.store.write(listOf(started("b1"), result))
        env.store.write(listOf(result))

        val build = assertIs<BuildSnapshot.Finished>(env.builds().single())
        assertEquals(1, build.outcome.errors.size)
    }

    @Test
    fun resultForAnUnknownBuild_addsItAsFinished() = runTest {
        val env = env()
        env.store.write(listOf(result("b9", FinishStatus.SUCCESS)))

        assertIs<BuildSnapshot.Finished>(env.builds().single())
    }

    @Test
    fun snapshot_dropsStaleRunningBuilds_butKeepsHistory() = runTest {
        val env = env()
        env.store.write(listOf(started("old-run"), started("done"), result("done", FinishStatus.SUCCESS)))
        env.store.write(
            listOf(
                SnapshotEvent(
                    activeBuilds = listOf(SnapshotBuild("live", "app", startedAt = 50, currentTask = ":x")),
                    recentResults = emptyList(),
                ),
            ),
        )

        val byId = env.builds().associateBy { it.buildId }
        assertEquals(setOf("live", "done"), byId.keys)
        assertEquals(":x", assertIs<BuildSnapshot.Active>(byId.getValue("live")).currentTask)
    }

    @Test
    fun emptySnapshot_keepsFinishedHistory() = runTest {
        val env = env()
        env.store.write(listOf(started("done"), result("done", FinishStatus.SUCCESS)))
        env.store.write(listOf(SnapshotEvent(activeBuilds = emptyList(), recentResults = emptyList())))

        assertEquals(listOf("done"), env.builds().map { it.buildId })
    }

    @Test
    fun history_isCappedAndOldBuildsLoseTheirLines() = runTest {
        val env = env(historyLimit = 2)
        (1..3).forEach { i ->
            env.store.write(
                listOf(
                    started("b$i", startedAt = i.toLong()),
                    TaskStartedEvent("b$i", ":t", i.toLong()),
                    result("b$i", FinishStatus.SUCCESS, startedAt = i.toLong()),
                ),
            )
        }

        assertEquals(setOf("b2", "b3"), env.builds().map { it.buildId }.toSet())
        assertEquals(0L, env.db.buildLogQueries.logCount("b1").executeAsOne())
    }

    @Test
    fun dropActiveBuilds_removesOnlyRunningOnes() = runTest {
        val env = env()
        env.store.write(listOf(started("run"), started("done"), result("done", FinishStatus.SUCCESS)))

        env.store.dropActiveBuilds()

        assertEquals(listOf("done"), env.builds().map { it.buildId })
        assertNull(env.db.activeBuildQueries.selectStatus("run").executeAsOneOrNull())
    }

    private class Env(
        val db: ActiveBuildsDatabase,
        val store: SqlBuildEventStore,
        private val buildSource: LocalActiveBuildSource,
        private val logSource: LocalBuildLogSource,
        private val statsSource: LocalLogStatsSource,
    ) {
        suspend fun builds() = buildSource.observe(Unit).first()
        suspend fun logs(query: LogQuery) = logSource.observe(query).first()
        suspend fun stats(buildId: String) = statsSource.observe(buildId).first()
    }

    private fun TestScope.env(historyLimit: Int = 30): Env {
        val db = createTestActiveBuildsDatabase()
        val dispatchers = FakeAppDispatchers(UnconfinedTestDispatcher(testScheduler))
        return Env(
            db = db,
            store = SqlBuildEventStore(db, dispatchers, historyLimit),
            buildSource = LocalActiveBuildSource(
                db.activeBuildQueries,
                db.buildIssueQueries,
                BuildSnapshotMapper(),
                dispatchers,
            ),
            logSource = LocalBuildLogSource(db.buildLogQueries, LogRecordMapper(), dispatchers),
            statsSource = LocalLogStatsSource(db.buildLogCountQueries, dispatchers),
        )
    }

    private fun started(id: String, startedAt: Long = 1) = BuildStartedEvent(id, "app", startedAt)

    private fun result(
        id: String,
        status: FinishStatus,
        errors: List<BuildIssue> = emptyList(),
        startedAt: Long = 1,
    ) = BuildResultEvent(
        buildId = id,
        projectName = "app",
        status = status,
        durationMs = 1_000,
        startedAt = startedAt,
        finishedAt = startedAt + 1_000,
        errors = errors,
        warnings = emptyList(),
    )
}
