package me.yuriisoft.buildnotify.konsist

/**
 * The project's structural rules as pure functions over [SourceFile]s. Each returns the
 * violations as stable strings (`path` or `path:line: detail`) for [Ratchet.check].
 */
object ArchitectureRules {

    private val topLevelType = Regex(
        """^(?:@\w+(?:\([^)]*\))?\s+)*(?:(?:public|internal|private|protected|sealed|data|enum|abstract|""" +
            """open|value|annotation|inline|fun)\s+)*(?:class|interface|object)\s+[A-Z]""",
    )

    /** Files that declare more than [max] top-level classes, interfaces or objects. */
    fun manyTypesPerFile(files: List<SourceFile>, max: Int = 1): List<String> =
        files.filter { file -> file.lines.count { topLevelType.containsMatchIn(it) && !it.startsWith(" ") } > max }
            .map { it.path }

    /** Files longer than [maxLines] lines. */
    fun longFiles(files: List<SourceFile>, maxLines: Int): List<String> =
        files.filter { it.lines.size > maxLines }.map { it.path }

    /** Imports starting with any of [prefixes], reported as `path: import`. */
    fun forbiddenImports(files: List<SourceFile>, prefixes: List<String>): List<String> =
        files.flatMap { file ->
            file.imports.filter { import -> prefixes.any { import.startsWith(it) } }.map { "${file.path}: $it" }
        }

    /** Code (not comments) matching any of [patterns], reported as `path:line: match`. */
    fun forbiddenCode(files: List<SourceFile>, patterns: List<Regex>): List<String> =
        files.flatMap { file ->
            file.lines.withIndex()
                .filterNot { (_, line) -> line.isCommentOrImport() }
                .flatMap { (index, line) ->
                    patterns.mapNotNull { pattern -> pattern.find(line)?.let { "${file.path}:${index + 1}: ${it.value}" } }
                }
        }

    /**
     * `when` branches that name an object of a sealed hierarchy without `is`
     * (`State.Loading ->` instead of `is State.Loading ->`). Enum entries are not affected:
     * only objects declared inside a `sealed class` / `sealed interface` count.
     */
    fun whenBranchesWithoutIs(files: List<SourceFile>): List<String> =
        WhenBranches(sealedObjects(files)).violations(files)

    private fun sealedObjects(files: List<SourceFile>): Set<String> {
        val sealed = Regex("""\bsealed\s+(?:class|interface)\s+(\w+)""")
        val objectDecl = Regex("""\bobject\s+(\w+)""")
        return files.flatMapTo(mutableSetOf()) { file ->
            sealed.findAll(file.text).flatMap { match ->
                val body = blockAfter(file.text, match.range.last) ?: return@flatMap emptySequence()
                objectDecl.findAll(body).map { "${match.groupValues[1]}.${it.groupValues[1]}" }
            }
        }
    }

    /** Body of the declaration whose header ends at [from], or `null` if it has none. */
    private fun blockAfter(text: String, from: Int): String? {
        val open = text.indexOf('{', from)
        if (open == -1) return null
        val header = text.substring(from, open)
        if (header.count { it == '\n' } > MAX_HEADER_LINES) return null
        var depth = 0
        for (i in open until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return text.substring(open + 1, i)
            }
        }
        return null
    }

    /** Feature packages a file imports, other than its own (`feature.discovery` → `feature.activebuilds`). */
    fun crossFeatureImports(files: List<SourceFile>, featureRoot: String): List<String> =
        files.flatMap { file ->
            val own = file.packageName.removePrefix("$featureRoot.").substringBefore('.')
            if (!file.packageName.startsWith("$featureRoot.")) return@flatMap emptyList()
            file.imports.filter { it.startsWith("$featureRoot.") && !it.startsWith("$featureRoot.$own.") }
                .map { "${file.path}: $it" }
        }

    /**
     * Clean-architecture direction inside one feature: for a file whose package contains
     * layer `.X.`, imports of the same feature's layers in [forbidden][X] are violations.
     * Files matching [exempt] (e.g. `*Screen.kt` entry points) are skipped.
     */
    fun layerViolations(
        files: List<SourceFile>,
        featureRoot: String,
        forbidden: Map<String, List<String>>,
        exempt: (SourceFile) -> Boolean = { false },
    ): List<String> = files.filterNot(exempt).flatMap { file ->
        if (!file.packageName.startsWith("$featureRoot.")) return@flatMap emptyList()
        val feature = "$featureRoot." + file.packageName.removePrefix("$featureRoot.").substringBefore('.')
        val layer = file.packageName.removePrefix("$feature.").substringBefore('.')
        val banned = forbidden[layer].orEmpty()
        file.imports.filter { import -> banned.any { import.startsWith("$feature.$it.") } }.map { "${file.path}: $it" }
    }

    private fun String.isCommentOrImport(): Boolean {
        val trimmed = trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*") || startsWith("import ")
    }

    private const val MAX_HEADER_LINES = 3
}
