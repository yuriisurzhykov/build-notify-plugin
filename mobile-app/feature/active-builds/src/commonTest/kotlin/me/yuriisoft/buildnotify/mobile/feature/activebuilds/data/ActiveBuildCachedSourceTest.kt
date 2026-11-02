package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeLocalActiveBuildSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeRemoteActiveBuildSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActiveBuildCachedSourceTest {

    private val cachedBuild = BuildSnapshot.Finished(
        buildId = "b1",
        projectName = "app",
        startedAt = 1L,
        outcome = BuildOutcome(FinishStatus.FAILED, 10L, emptyList(), emptyList()),
    )

    @Test
    fun leadingEmptyRemoteEmission_doesNotWipeCachedHistory() = runTest {
        val remote = FakeRemoteActiveBuildSource()
        val local = FakeLocalActiveBuildSource().apply { stored.value = listOf(cachedBuild) }
        val source = ActiveBuildCachedSource(remote, local)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.observe(Unit).collect { }
        }
        remote.emit(emptyList())

        assertEquals(listOf<BuildSnapshot>(cachedBuild), local.stored.value)
    }

    @Test
    fun emptyEmissionAfterRealData_isPersisted() = runTest {
        val remote = FakeRemoteActiveBuildSource()
        val local = FakeLocalActiveBuildSource().apply { stored.value = listOf(cachedBuild) }
        val source = ActiveBuildCachedSource(remote, local)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.observe(Unit).collect { }
        }
        remote.emit(emptyList())
        remote.emit(listOf(cachedBuild.copy(buildId = "b2")))
        remote.emit(emptyList())

        assertTrue(local.stored.value.isEmpty())
    }
}
