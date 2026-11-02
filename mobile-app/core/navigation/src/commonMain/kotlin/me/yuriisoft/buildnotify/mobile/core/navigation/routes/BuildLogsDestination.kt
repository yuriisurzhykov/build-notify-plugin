package me.yuriisoft.buildnotify.mobile.core.navigation.routes

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import me.yuriisoft.buildnotify.mobile.core.navigation.Destination

private const val BUILD_LOGS_BASE = "build_logs"

/**
 * Full-screen log viewer for a single build.
 *
 * Shared in `core:navigation` so that platform code (e.g. a tapped Android
 * notification) can open the logs of a build without depending on the feature.
 */
object BuildLogsDestination : Destination(route = "$BUILD_LOGS_BASE/{buildId}") {

    const val ARG_BUILD_ID: String = "buildId"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_BUILD_ID) { type = NavType.StringType },
    )

    /** Concrete route for [buildId]; reserved URI characters are percent-encoded. */
    fun routeFor(buildId: String): String = "$BUILD_LOGS_BASE/${encodePathSegment(buildId)}"
}

private fun encodePathSegment(value: String): String = buildString {
    value.forEach { char ->
        if (char.isLetterOrDigit() || char in UNRESERVED) {
            append(char)
        } else {
            char.toString().encodeToByteArray().forEach { byte ->
                append('%')
                append(HEX[(byte.toInt() shr 4) and 0x0F])
                append(HEX[byte.toInt() and 0x0F])
            }
        }
    }
}

private const val UNRESERVED = "-._~:"
private const val HEX = "0123456789ABCDEF"
