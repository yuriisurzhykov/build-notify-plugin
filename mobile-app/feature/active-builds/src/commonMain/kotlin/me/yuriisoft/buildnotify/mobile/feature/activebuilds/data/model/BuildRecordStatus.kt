package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.model

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus

enum class BuildRecordStatus {
    ACTIVE,
    SUCCESS,
    FAILED,
    CANCELLED,
    ;

    companion object {
        fun fromDomainFinish(status: FinishStatus): BuildRecordStatus =
            when (status) {
                FinishStatus.SUCCESS   -> SUCCESS
                FinishStatus.FAILED    -> FAILED
                FinishStatus.CANCELLED -> CANCELLED
            }
    }
}
