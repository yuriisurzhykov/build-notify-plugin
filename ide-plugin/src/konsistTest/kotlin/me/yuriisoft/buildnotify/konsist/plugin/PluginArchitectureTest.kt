package me.yuriisoft.buildnotify.konsist.plugin

import me.yuriisoft.buildnotify.konsist.ArchitectureRules
import me.yuriisoft.buildnotify.konsist.Ratchet
import kotlin.test.Test
import kotlin.test.fail

/** Structural rules of the IDE plugin; same limits as mobile-app. */
class PluginArchitectureTest {

    @Test
    fun `every model lives in its own file`() = ratchet(
        "one top-level type per file",
        ArchitectureRules.manyTypesPerFile(PluginSources.production),
        known = emptyList(),
    )

    @Test
    fun `production files stay short`() = ratchet(
        "production file <= $MAX_FILE_LINES lines",
        ArchitectureRules.longFiles(PluginSources.production, MAX_FILE_LINES),
        known = listOf(
            "src/main/kotlin/me/yuriisoft/buildnotify/network/server/BuildWebSocketServer.kt",
            "src/main/kotlin/me/yuriisoft/buildnotify/settings/PluginSettingsConfigurable.kt",
        ),
    )

    @Test
    fun `when branches on sealed objects use is`() = ratchet(
        "`is` for sealed objects in when",
        ArchitectureRules.whenBranchesWithoutIs(PluginSources.production + PluginSources.tests),
        known = emptyList(),
    )

    @Test
    fun `the wire protocol does not depend on the IDE`() = ratchet(
        "serialization is platform-free",
        ArchitectureRules.forbiddenImports(PluginSources.inPackage(".serialization"), listOf("com.intellij")),
        known = emptyList(),
    )

    @Test
    fun `security code has no UI`() = ratchet(
        "security is UI-free",
        ArchitectureRules.forbiddenImports(
            PluginSources.inPackage(".security"),
            listOf("javax.swing", "com.intellij.openapi.ui", "me.yuriisoft.buildnotify.network.server.ui"),
        ),
        known = emptyList(),
    )

    @Test
    fun `build processing does not know the transport`() = ratchet(
        "build mapping/pipeline independent of network",
        ArchitectureRules.forbiddenImports(
            PluginSources.production.filter {
                it.packageName.contains(".build.mapper") || it.packageName.contains(".build.pipeline")
            },
            listOf("me.yuriisoft.buildnotify.network"),
        ),
        known = emptyList(),
    )

    private fun ratchet(rule: String, found: List<String>, known: List<String>) {
        Ratchet.check(rule, found, known)?.let { fail(it) }
    }

    private companion object {
        const val MAX_FILE_LINES = 250
    }
}
