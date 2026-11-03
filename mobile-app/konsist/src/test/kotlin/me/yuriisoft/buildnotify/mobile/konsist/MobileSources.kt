package me.yuriisoft.buildnotify.mobile.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import me.yuriisoft.buildnotify.konsist.SourceFile

/** Konsist scopes of the mobile project, as [SourceFile]s with paths relative to `mobile-app/`. */
object MobileSources {

    val production: List<SourceFile> by lazy { Konsist.scopeFromProduction().files.map(::toSource) }

    /** Tests of the app modules; this module's own fixtures contain violations on purpose. */
    val tests: List<SourceFile> by lazy {
        Konsist.scopeFromTest().files.map(::toSource).filterNot { it.path.startsWith("konsist/") }
    }

    /** Feature and app code: everything that must style itself only through Graphite. */
    val styled: List<SourceFile> by lazy {
        production.filter {
            (it.path.startsWith("feature/") && !it.path.startsWith("feature/ui-catalog/")) ||
                it.path.startsWith("app/") ||
                it.path.startsWith("androidApp/")
        }
    }

    const val FEATURE_ROOT = "me.yuriisoft.buildnotify.mobile.feature"

    private fun toSource(file: KoFileDeclaration): SourceFile =
        SourceFile(path = file.path.replace('\\', '/').substringAfter("/mobile-app/"), text = file.text)
}
