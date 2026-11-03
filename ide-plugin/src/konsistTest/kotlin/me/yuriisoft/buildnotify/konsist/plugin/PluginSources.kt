package me.yuriisoft.buildnotify.konsist.plugin

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import me.yuriisoft.buildnotify.konsist.SourceFile

/** Konsist scopes of the IDE plugin, as [SourceFile]s with paths relative to `ide-plugin/`. */
object PluginSources {

    val production: List<SourceFile> by lazy { Konsist.scopeFromProduction().files.map(::toSource) }

    val tests: List<SourceFile> by lazy {
        Konsist.scopeFromTest().files.map(::toSource).filterNot { it.path.startsWith("src/konsistTest/") }
    }

    fun inPackage(suffix: String): List<SourceFile> = production.filter { it.packageName.endsWith(suffix) }

    private fun toSource(file: KoFileDeclaration): SourceFile =
        SourceFile(path = file.path.replace('\\', '/').substringAfter("/ide-plugin/"), text = file.text)
}
