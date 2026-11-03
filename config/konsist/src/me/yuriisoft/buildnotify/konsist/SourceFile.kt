package me.yuriisoft.buildnotify.konsist

/**
 * A Kotlin source file as the architecture rules see it: a path relative to the build
 * root (`feature/discovery/src/...`) and its text. Built from Konsist's scope so the
 * rules work on production or test sources of any Kotlin Multiplatform source set.
 */
data class SourceFile(
    val path: String,
    val text: String,
) {
    val lines: List<String> by lazy { text.lines() }

    val imports: List<String> by lazy {
        lines.filter { it.startsWith("import ") }.map { it.removePrefix("import ").substringBefore(" as ").trim() }
    }

    val packageName: String by lazy {
        lines.firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim().orEmpty()
    }

    val fileName: String get() = path.substringAfterLast('/')
}
