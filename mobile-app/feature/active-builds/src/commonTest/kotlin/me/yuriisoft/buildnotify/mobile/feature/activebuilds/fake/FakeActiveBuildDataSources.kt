package me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

class FakeRemoteActiveBuildSource : ReadableDataSource<Unit, List<BuildSnapshot>> {
    private val flow = MutableSharedFlow<List<BuildSnapshot>>(extraBufferCapacity = 16)
    override fun observe(params: Unit): Flow<List<BuildSnapshot>> = flow
    suspend fun emit(data: List<BuildSnapshot>) {
        flow.emit(data)
    }
}

class FakeLocalActiveBuildSource : MutableDataSource<Unit, List<BuildSnapshot>> {
    val stored = MutableStateFlow<List<BuildSnapshot>>(emptyList())
    override fun observe(params: Unit): Flow<List<BuildSnapshot>> = stored
    override suspend fun save(params: Unit, data: List<BuildSnapshot>) {
        stored.value = data
    }
    override suspend fun delete(params: Unit) {
        stored.value = emptyList()
    }
}

class FakeRemoteBuildLogSource : ReadableDataSource<String, List<BuildLogEntry>> {
    private val flows = mutableMapOf<String, MutableSharedFlow<List<BuildLogEntry>>>()
    private fun flowFor(buildId: String): MutableSharedFlow<List<BuildLogEntry>> =
        flows.getOrPut(buildId) { MutableSharedFlow(extraBufferCapacity = 16) }
    override fun observe(params: String): Flow<List<BuildLogEntry>> = flowFor(params)
    suspend fun emit(buildId: String, data: List<BuildLogEntry>) {
        flowFor(buildId).emit(data)
    }
}

class FakeLocalBuildLogSource : MutableDataSource<String, List<BuildLogEntry>> {
    private val stored = MutableStateFlow<Map<String, List<BuildLogEntry>>>(emptyMap())
    override fun observe(params: String): Flow<List<BuildLogEntry>> =
        stored.map { it[params].orEmpty() }
    override suspend fun save(params: String, data: List<BuildLogEntry>) {
        stored.value = stored.value + (params to data)
    }
    override suspend fun delete(params: String) {
        stored.value = stored.value - params
    }
}
