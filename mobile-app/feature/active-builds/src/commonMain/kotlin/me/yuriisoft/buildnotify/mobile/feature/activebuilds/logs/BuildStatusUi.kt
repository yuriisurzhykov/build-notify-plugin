package me.yuriisoft.buildnotify.mobile.feature.activebuilds.logs

import androidx.compose.runtime.Immutable
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus

@Immutable
sealed interface BuildStatusUi {

    @Immutable
    data class Running(val startedAt: Long, val currentTask: String?) : BuildStatusUi

    @Immutable
    data class Finished(val status: FinishStatus, val durationMs: Long) : BuildStatusUi

    /** Logs are cached but the build row is gone. */
    @Immutable
    data object Unknown : BuildStatusUi
}
