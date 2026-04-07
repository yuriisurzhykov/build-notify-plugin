package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.mapper

import me.yuriisoft.buildnotify.mobile.core.mapper.Mapper
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model.BuildIssueRecord
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue

class IssueRecordMapper : Mapper<BuildIssueRecord, BuildIssue> {

    override fun map(from: BuildIssueRecord): BuildIssue = BuildIssue(
        message = from.message,
        filePath = from.filePath,
        line = from.line,
    )
}
