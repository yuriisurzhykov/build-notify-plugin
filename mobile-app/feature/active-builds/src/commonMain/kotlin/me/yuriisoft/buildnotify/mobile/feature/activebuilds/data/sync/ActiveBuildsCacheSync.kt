package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.repository.IActiveBuildRepository

/**
 * Keeps the local build cache current for the whole process lifetime.
 *
 * The cached data sources write remote data to SQLite only while somebody collects
 * them. Without this class, a build that ran while no screen was open would exist only
 * in memory, and its logs would be gone after the process died — exactly when the
 * user taps a build notification to read them.
 *
 * Collects the build list and the log stream of every known build; the collection
 * itself is what drives persistence, the values are discarded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ActiveBuildsCacheSync(
    repository: IActiveBuildRepository,
    dispatchers: AppDispatchers,
    scope: CoroutineScope,
) {
    init {
        dispatchers.launchBackground(scope) {
            repository.observeBuilds()
                .map { builds -> builds.map { it.buildId }.toSet() }
                .distinctUntilChanged()
                .flatMapLatest { buildIds ->
                    if (buildIds.isEmpty()) emptyFlow()
                    else merge(*buildIds.map(repository::observeLogs).toTypedArray())
                }
                .collect { }
        }
    }
}
