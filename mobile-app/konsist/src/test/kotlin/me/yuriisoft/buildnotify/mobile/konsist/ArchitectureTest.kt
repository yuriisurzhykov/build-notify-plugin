package me.yuriisoft.buildnotify.mobile.konsist

import me.yuriisoft.buildnotify.konsist.ArchitectureRules
import me.yuriisoft.buildnotify.konsist.Ratchet
import me.yuriisoft.buildnotify.mobile.konsist.MobileSources.FEATURE_ROOT
import kotlin.test.Test
import kotlin.test.fail

/** Structural rules of the mobile project. Limits are deliberate: change them in review only. */
class ArchitectureTest {

    @Test
    fun `every model lives in its own file`() = ratchet(
        "one top-level type per file",
        ArchitectureRules.manyTypesPerFile(MobileSources.production),
        KnownViolations.manyTypesPerFile,
    )

    @Test
    fun `production files stay short`() = ratchet(
        "production file <= $MAX_FILE_LINES lines",
        ArchitectureRules.longFiles(MobileSources.production, MAX_FILE_LINES),
        KnownViolations.longFiles,
    )

    @Test
    fun `test files stay short`() = ratchet(
        "test file <= $MAX_TEST_FILE_LINES lines",
        ArchitectureRules.longFiles(MobileSources.tests, MAX_TEST_FILE_LINES),
        KnownViolations.longTestFiles,
    )

    @Test
    fun `when branches on sealed objects use is`() = ratchet(
        "`is` for sealed objects in when",
        ArchitectureRules.whenBranchesWithoutIs(MobileSources.production + MobileSources.tests),
        known = emptyList(),
    )

    @Test
    fun `features and app style themselves only through Graphite`() = ratchet(
        "no raw styling outside the design system",
        ArchitectureRules.forbiddenImports(MobileSources.styled, RAW_STYLING_IMPORTS) +
            ArchitectureRules.forbiddenCode(MobileSources.styled, RAW_STYLING_CODE),
        known = emptyList(),
    )

    @Test
    fun `the design system depends on nothing in the app`() = ratchet(
        "design system is a leaf",
        ArchitectureRules.forbiddenImports(
            MobileSources.production.filter { it.path.startsWith("core/designsystem/") },
            listOf(
                "me.yuriisoft.buildnotify.mobile.feature",
                "me.yuriisoft.buildnotify.mobile.network",
                "me.yuriisoft.buildnotify.mobile.core.",
                "me.yuriisoft.buildnotify.mobile.ui.",
            ),
        ),
        known = emptyList(),
    )

    @Test
    fun `legacy core ui is used only for resources`() = ratchet(
        "core:ui only for TextResource / ImageResource",
        ArchitectureRules.forbiddenImports(
            MobileSources.production.filterNot {
                it.path.startsWith("core/ui/") || it.path.startsWith("feature/ui-catalog/")
            },
            listOf("me.yuriisoft.buildnotify.mobile.ui."),
        ).filterNot { it.contains(".ui.resource.") || it.endsWith(".ui.theme.BuildNotifyTheme") },
        KnownViolations.legacyUiImports,
    )

    @Test
    fun `features do not depend on each other`() = ratchet(
        "no feature-to-feature imports",
        ArchitectureRules.crossFeatureImports(MobileSources.production, FEATURE_ROOT),
        known = emptyList(),
    )

    @Test
    fun `layers point inwards`() = ratchet(
        "clean architecture layering",
        ArchitectureRules.layerViolations(MobileSources.production, FEATURE_ROOT, LAYER_RULES) {
            // A Screen is the presentation entry point that renders its ui content.
            it.fileName.endsWith("Screen.kt")
        },
        known = emptyList(),
    )

    private fun ratchet(rule: String, found: List<String>, known: List<String>) {
        Ratchet.check(rule, found, known)?.let { fail(it) }
    }

    private companion object {
        const val MAX_FILE_LINES = 250
        const val MAX_TEST_FILE_LINES = 400

        val RAW_STYLING_IMPORTS = listOf(
            "androidx.compose.animation",
            "androidx.compose.material",
            "androidx.compose.foundation.shape",
            "androidx.compose.ui.text.font",
            "androidx.compose.ui.unit.sp",
            "androidx.compose.ui.text.TextStyle",
        )

        val RAW_STYLING_CODE = listOf(
            Regex("""Color\(0x"""),
            Regex("""\.sp\b"""),
            Regex("""(?<![\w.])TextStyle\("""),
        )

        /** For a file in layer `key`, imports of the same feature's layers in `value` are forbidden. */
        val LAYER_RULES = mapOf(
            "domain" to listOf("data", "presentation", "logs", "ui", "di"),
            "data" to listOf("presentation", "logs", "ui", "di"),
            "presentation" to listOf("data", "ui", "di"),
            "logs" to listOf("data", "ui", "di"),
            "ui" to listOf("data", "di"),
        )
    }
}
