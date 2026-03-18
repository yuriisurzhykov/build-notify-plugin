package me.yuriisoft.buildnotify.build

import me.yuriisoft.buildnotify.build.model.BuildIssue

/**
 * Parses raw Gradle build output lines into structured [BuildIssue] instances.
 *
 * Handles both Kotlin and Java compiler output formats:
 *
 * Kotlin: `e: /path/File.kt: (10, 5): Unresolved reference: foo`
 *         `w: /path/File.kt: (3, 1): Unused variable`
 *
 * Java:   `/path/File.java:10: error: cannot find symbol`
 *         `/path/File.java:3: warning: unchecked cast`
 *
 * Stateless object — all methods are pure functions.
 */
object BuildOutputParser {

    // Kotlin compiler: "e: /path/File.kt: (line, col): message"
    // The prefix "e:" = error, "w:" = warning.
    private val KOTLIN_PATTERN = Regex(
        """^([ew]):\s+(.+\.kt):\s+\((\d+),\s*(\d+)\):\s+(.+)$"""
    )

    // Java compiler: "/path/File.java:line: error: message"
    // or:           "/path/File.java:line: warning: message"
    private val JAVA_PATTERN = Regex(
        """^(.+\.java):(\d+):\s+(error|warning):\s+(.+)$"""
    )

    // Gradle task failure marker — signals something meaningful in the next lines.
    // Not parsed directly but used to filter noise.
    private val GRADLE_ERROR_HEADER = Regex("""^(FAILURE|> Task .+ FAILED)""")

    /**
     * Attempts to parse [line] as a compiler diagnostic.
     * Returns a [BuildIssue] if the line matches a known format, null otherwise.
     */
    fun parseLine(line: String): BuildIssue? {
        return parseKotlinLine(line) ?: parseJavaLine(line)
    }

    private fun parseKotlinLine(line: String): BuildIssue? {
        val match = KOTLIN_PATTERN.find(line.trim()) ?: return null
        val (prefix, path, lineStr, colStr, message) = match.destructured
        return BuildIssue(
            filePath = path,
            line = lineStr.toIntOrNull() ?: -1,
            column = colStr.toIntOrNull() ?: -1,
            message = message.trim(),
            severity = if (prefix == "e") BuildIssue.Severity.ERROR else BuildIssue.Severity.WARNING,
        )
    }

    private fun parseJavaLine(line: String): BuildIssue? {
        val match = JAVA_PATTERN.find(line.trim()) ?: return null
        val (path, lineStr, kind, message) = match.destructured
        return BuildIssue(
            filePath = path,
            line = lineStr.toIntOrNull() ?: -1,
            column = -1, // Java compiler does not emit column in standard output
            message = message.trim(),
            severity = if (kind == "error") BuildIssue.Severity.ERROR else BuildIssue.Severity.WARNING,
        )
    }
}