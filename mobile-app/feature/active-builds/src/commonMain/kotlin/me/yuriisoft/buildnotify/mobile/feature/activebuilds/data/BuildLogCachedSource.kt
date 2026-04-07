package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data

import me.yuriisoft.buildnotify.mobile.core.cache.source.CachedReadableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.MutableDataSource
import me.yuriisoft.buildnotify.mobile.core.cache.source.ReadableDataSource
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry

class BuildLogCachedSource(
    remote: ReadableDataSource<String, List<BuildLogEntry>>,
    local: MutableDataSource<String, List<BuildLogEntry>>,
) : CachedReadableDataSource<String, List<BuildLogEntry>>(remote, local)
