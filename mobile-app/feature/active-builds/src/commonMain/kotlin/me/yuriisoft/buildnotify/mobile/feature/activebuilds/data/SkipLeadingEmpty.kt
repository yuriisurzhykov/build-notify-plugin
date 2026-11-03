package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

internal class SkipLeadingEmpty(
    private val origin: ReadableDataSource<Unit, List<BuildSnapshot>>,
) : ReadableDataSource<Unit, List<BuildSnapshot>> {

    override fun observe(params: Unit): Flow<List<BuildSnapshot>> =
        origin.observe(params).dropWhile { it.isEmpty() }
}
