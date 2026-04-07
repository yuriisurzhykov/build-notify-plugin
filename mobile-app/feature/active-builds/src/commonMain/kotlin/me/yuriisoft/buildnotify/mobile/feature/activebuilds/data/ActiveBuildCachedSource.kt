package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import me.yuriisoft.buildnotify.mobile.core.cache.source.CachedReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot

class ActiveBuildCachedSource(
    remote: ReadableDataSource<Unit, List<BuildSnapshot>>,
    local: MutableDataSource<Unit, List<BuildSnapshot>>,
) : CachedReadableDataSource<Unit, List<BuildSnapshot>>(remote, local)
