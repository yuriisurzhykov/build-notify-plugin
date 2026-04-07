package me.yuriisoft.buildnotify.mobile.feature.activebuilds.data.sync

import kotlinx.coroutines.test.runTest
import me.yuriisoft.buildnotify.mobile.data.protocol.CancelBuildCommand
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake.FakeActiveSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultCancelBuildUseCaseTest {

    @Test
    fun invoke_sendsCancelBuildCommandEnvelope() = runTest {
        val session = FakeActiveSession()
        val useCase = DefaultCancelBuildUseCase(session)

        useCase("build-42")

        val envelope = session.sentEnvelopes.single()
        val payload = assertIs<CancelBuildCommand>(envelope.payload)
        assertEquals("build-42", payload.buildId)
    }
}
