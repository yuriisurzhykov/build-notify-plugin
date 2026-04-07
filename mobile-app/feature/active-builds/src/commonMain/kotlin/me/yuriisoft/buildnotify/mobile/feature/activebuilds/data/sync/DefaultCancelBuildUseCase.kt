package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import me.yuriisoft.buildnotify.mobile.data.protocol.CancelBuildCommand
import me.yuriisoft.buildnotify.mobile.data.protocol.WsEnvelope
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.usecase.CancelBuildUseCase
import me.yuriisoft.buildnotify.mobile.network.connection.ActiveSession

class DefaultCancelBuildUseCase(
    private val session: ActiveSession,
) : CancelBuildUseCase {

    override suspend fun invoke(buildId: String) {
        session.send(WsEnvelope(payload = CancelBuildCommand(buildId)))
    }
}
