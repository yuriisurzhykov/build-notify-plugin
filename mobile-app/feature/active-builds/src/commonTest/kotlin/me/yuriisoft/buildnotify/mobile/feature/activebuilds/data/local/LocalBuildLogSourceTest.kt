package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.LogRecordMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildRecordStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.db.ActiveBuildsDatabase
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalBuildLogSourceTest {

    @Test
    fun observeBuildId_emitsEmptyListInitially() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        val source = localLogSource(db)

        assertEquals(emptyList(), source.observe("b1").first())
    }

    @Test
    fun save_appendsNewEntriesOnly() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        val source = localLogSource(db)

        val firstBatch = listOf(
            BuildLogEntry(10L, "task1", LogKind.TASK),
            BuildLogEntry(11L, "warn1", LogKind.WARNING),
        )
        source.save("b1", firstBatch)
        assertEquals(firstBatch, source.observe("b1").first())

        val extended = firstBatch + BuildLogEntry(12L, "err1", LogKind.ERROR)
        source.save("b1", extended)

        assertEquals(extended, source.observe("b1").first())
    }

    @Test
    fun save_withSameDataTwice_doesNotDuplicateEntries() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        val source = localLogSource(db)

        val entries = listOf(BuildLogEntry(1L, "m", LogKind.TASK))
        source.save("b1", entries)
        source.save("b1", entries)

        assertEquals(entries, source.observe("b1").first())
        assertEquals(1L, db.buildLogQueries.logCount("b1").executeAsOne())
    }

    @Test
    fun deleteBuildId_removesAllLogsForThatBuild() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        val source = localLogSource(db)

        source.save(
            "b1",
            listOf(
                BuildLogEntry(1L, "a", LogKind.TASK),
                BuildLogEntry(2L, "b", LogKind.TASK),
            ),
        )
        assertEquals(2, source.observe("b1").first().size)

        source.delete("b1")

        assertEquals(emptyList(), source.observe("b1").first())
    }

    @Test
    fun logsAreOrderedByInsertionOrder() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        val source = localLogSource(db)

        val entries = listOf(
            BuildLogEntry(100L, "first", LogKind.TASK),
            BuildLogEntry(200L, "second", LogKind.ERROR),
            BuildLogEntry(300L, "third", LogKind.WARNING),
        )
        source.save("b1", entries)

        assertEquals(
            listOf("first", "second", "third"),
            source.observe("b1").first().map { it.message },
        )
    }

    @Test
    fun observeIsIsolatedPerBuildId() = runTest {
        val db = createTestActiveBuildsDatabase()
        seedActiveBuild(db, "b1")
        seedActiveBuild(db, "b2")
        val source = localLogSource(db)

        source.save("b1", listOf(BuildLogEntry(1L, "only-b1", LogKind.TASK)))
        source.save("b2", listOf(BuildLogEntry(2L, "only-b2", LogKind.TASK)))

        assertEquals(listOf("only-b1"), source.observe("b1").first().map { it.message })
        assertEquals(listOf("only-b2"), source.observe("b2").first().map { it.message })
    }

    private fun seedActiveBuild(db: ActiveBuildsDatabase, buildId: String) {
        db.activeBuildQueries.upsert(
            build_id = buildId,
            project_name = "proj",
            status = BuildRecordStatus.ACTIVE.name,
            started_at = 0L,
            current_task = null,
            duration_ms = null,
        )
    }

    private fun TestScope.localLogSource(db: ActiveBuildsDatabase): LocalBuildLogSource =
        LocalBuildLogSource(
            queries = db.buildLogQueries,
            logMapper = LogRecordMapper(),
            dispatchers = FakeAppDispatchers(UnconfinedTestDispatcher(testScheduler)),
        )
}
