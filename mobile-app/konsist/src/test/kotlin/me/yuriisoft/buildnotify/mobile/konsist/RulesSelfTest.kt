package me.yuriisoft.buildnotify.mobile.konsist

import me.yuriisoft.buildnotify.konsist.ArchitectureRules
import me.yuriisoft.buildnotify.konsist.Ratchet
import me.yuriisoft.buildnotify.konsist.SourceFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** The rules themselves are code; make sure they catch what they claim to catch. */
class RulesSelfTest {

    private val fixture = SourceFile(
        path = "fixture/State.kt",
        text = """
            sealed interface State {
                data object Loading : State
                data class Loaded(val n: Int) : State
            }
            enum class Mode { Fast, Slow }
            fun render(s: State, m: Mode) {
                when (s) {
                    State.Loading -> Unit
                    is State.Loaded -> Unit
                }
                when (m) {
                    Mode.Fast -> Unit
                    Mode.Slow -> Unit
                }
                call(State.Loading, Mode.Fast)
            }
        """.trimIndent(),
    )

    @Test
    fun `when rule flags only sealed objects without is`() {
        assertEquals(
            listOf("fixture/State.kt:8: State.Loading"),
            ArchitectureRules.whenBranchesWithoutIs(listOf(fixture)),
        )
    }

    @Test
    fun `type rule counts top-level declarations only`() {
        assertEquals(listOf("fixture/State.kt"), ArchitectureRules.manyTypesPerFile(listOf(fixture)))
    }

    @Test
    fun `ratchet fails on new and on fixed violations`() {
        assertNull(Ratchet.check("r", found = listOf("a"), known = listOf("a")))
        assertNotNull(Ratchet.check("r", found = listOf("a", "b"), known = listOf("a")))
        assertNotNull(Ratchet.check("r", found = emptyList(), known = listOf("a")))
    }
}
