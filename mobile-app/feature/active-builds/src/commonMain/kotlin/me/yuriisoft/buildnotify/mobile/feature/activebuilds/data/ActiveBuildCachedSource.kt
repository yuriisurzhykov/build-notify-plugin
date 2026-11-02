package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import me.yuriisoft.buildnotify.mobile.core.cache.source.CachedReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

/**
 * Remote-first cache for the build list.
 *
 * The remote fold is seeded with an empty list before the IDE has said anything. Because
 * [LocalActiveBuildSource.save][me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.local.LocalActiveBuildSource.save]
 * has replace-all semantics, persisting that seed would wipe the cached history (and,
 * via `ON DELETE CASCADE`, every cached log) on each cold start — e.g. when the user
 * opens a build-result notification while the IDE is unreachable. Leading empty
 * emissions are therefore never written; stale rows are still dropped on the first
 * real snapshot and by `BuildCacheInvalidator` on disconnect.
 */
class ActiveBuildCachedSource(
    remote: ReadableDataSource<Unit, List<BuildSnapshot>>,
    local: MutableDataSource<Unit, List<BuildSnapshot>>,
) : CachedReadableDataSource<Unit, List<BuildSnapshot>>(SkipLeadingEmpty(remote), local)

private class SkipLeadingEmpty(
    private val origin: ReadableDataSource<Unit, List<BuildSnapshot>>,
) : ReadableDataSource<Unit, List<BuildSnapshot>> {

    override fun observe(params: Unit): Flow<List<BuildSnapshot>> =
        origin.observe(params).dropWhile { it.isEmpty() }
}
