package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildLogRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry

class LogRecordMapper : Mapper<BuildLogRecord, BuildLogEntry> {

    override fun map(from: BuildLogRecord): BuildLogEntry = BuildLogEntry(
        timestamp = from.timestamp,
        message = from.message,
        kind = from.kind.toDomain(),
        id = from.id,
    )
}
