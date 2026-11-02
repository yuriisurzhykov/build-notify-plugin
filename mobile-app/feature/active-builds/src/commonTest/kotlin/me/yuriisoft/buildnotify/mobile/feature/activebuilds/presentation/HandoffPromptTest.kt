package me.yuriisoft.buildnotify.mobile.feature.activebuilds.presentation

import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildIssue
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildLogEntry
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildOutcome
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.BuildSnapshot
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.FinishStatus
import me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.model.LogKind
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HandoffPromptTest {

    private val failed = BuildSnapshot.Finished(
        buildId = "b1",
        projectName = "payment-sdk",
        startedAt = 0,
        outcome = BuildOutcome(
            status = FinishStatus.FAILED,
            durationMs = 48_000,
            errors = listOf(BuildIssue("Unresolved reference: PaymentProcessor", "src/PaymentService.kt", 42)),
            warnings = listOf(BuildIssue("Deprecated", null, null)),
        ),
    )

    @Test
    fun prompt_listsErrorsWithLocations() {
        val prompt = HandoffPrompt.build(failed, logs = null)

        assertContains(prompt, "payment-sdk")
        assertContains(prompt, "src/PaymentService.kt:42")
        assertContains(prompt, "Unresolved reference: PaymentProcessor")
        assertContains(prompt, "1 warning(s)")
        assertFalse(prompt.contains("Build log"))
    }

    @Test
    fun prompt_attachesOnlyTheLogTail() {
        val logs = (1..(HandoffPrompt.MAX_LOG_LINES + 5)).map { BuildLogEntry(it.toLong(), "line $it", LogKind.TASK) }

        val prompt = HandoffPrompt.build(failed, logs)

        assertContains(prompt, "last ${HandoffPrompt.MAX_LOG_LINES} of ${logs.size} lines")
        assertFalse(prompt.contains("\nline 5\n"))
        assertContains(prompt, "line ${logs.size}")
    }

    @Test
    fun mapper_buildsHandoffForTheRequestedBuild() {
        val logs = listOf(BuildLogEntry(1, "e: boom", LogKind.ERROR))
        val state = ActiveBuildsUiMapper().map(
            snapshots = listOf(failed),
            logs = emptyMap(),
            handoff = HandoffRequest(buildId = "b1", includeLog = true, logs = logs),
        )

        val handoff = assertNotNull(assertIs<ActiveBuildsUiState.Loaded>(state).handoff)
        assertTrue(handoff.includeLog)
        assertEquals(1, handoff.logLineCount)
        assertContains(handoff.prompt, "e: boom")
    }
}
