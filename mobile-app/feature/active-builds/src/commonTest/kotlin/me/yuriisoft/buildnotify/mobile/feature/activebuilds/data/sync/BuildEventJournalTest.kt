package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStartedPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.HeartbeatPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStartedPayload
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper.BuildEventMapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.BuildStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.events.TaskStartedEvent
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeActiveSession
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeAppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeBuildEventStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class BuildEventJournalTest {

    @Test
    fun burstOfEvents_isWrittenAsOneBatch_andNonBuildPayloadsAreSkipped() = runTest {
        val (session, store) = journal()

        session.emit(BuildStartedPayload("b1", "app"))
        session.emit(HeartbeatPayload(serverTime = 1))
        session.emit(TaskStartedPayload("b1", "app", ":a"))
        session.emit(TaskStartedPayload("b1", "app", ":b"))
        settle()

        assertEquals(1, store.batches.size)
        assertEquals(
            listOf(BuildStartedEvent::class, TaskStartedEvent::class, TaskStartedEvent::class),
            store.batches.single().map { it::class },
        )
    }

    @Test
    fun failedWrite_losesOnlyItsBatch() = runTest {
        val (session, store) = journal()
        store.failNextWrite = IllegalStateException("disk full")

        session.emit(TaskStartedPayload("b1", "app", ":lost"))
        settle()
        session.emit(TaskStartedPayload("b1", "app", ":kept"))
        settle()

        val tasks = store.batches.flatten().map { (it as TaskStartedEvent).taskPath }
        assertEquals(listOf(":kept"), tasks)
    }

    private fun TestScope.journal(): Pair<FakeActiveSession, FakeBuildEventStore> {
        val session = FakeActiveSession()
        val store = FakeBuildEventStore()
        BuildEventJournal(
            session = session,
            mapper = BuildEventMapper(clock = { 0L }),
            store = store,
            dispatchers = FakeAppDispatchers(UnconfinedTestDispatcher(testScheduler)),
            scope = backgroundScope,
            window = WINDOW,
        )
        return session to store
    }

    private fun TestScope.settle() {
        advanceTimeBy(WINDOW * 2)
        runCurrent()
    }

    private companion object {
        val WINDOW = 100.milliseconds
    }
}
