package me.yuriisoft.buildnotify.mobile.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.yuriisoft.buildnotify.mobile.MainActivity
import me.yuriisoft.buildnotify.mobile.R
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildIssue
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResult
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildStatus
import me.yuriisoft.buildnotify.mobile.service.NotificationHelper.Companion.CHANNEL_BUILD_EVENTS
import me.yuriisoft.buildnotify.mobile.service.NotificationHelper.Companion.CHANNEL_PERSISTENT

/**
 * Owns all notification channels and builds every [Notification] the app shows.
 *
 * Two channels:
 *   - [CHANNEL_PERSISTENT] — low-priority, ongoing notification for the foreground service.
 *     While a build runs it shows the project and the current Gradle task.
 *   - [CHANNEL_BUILD_EVENTS] — high-priority heads-up notifications for build results.
 *     Tapping one opens the log of that build ([MainActivity.EXTRA_BUILD_ID]).
 *
 * Instantiated by [BuildMonitorService]; never outlives the service.
 */
class NotificationHelper(private val context: Context) {

    private val manager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    fun persistentNotification(
        message: String = context.getString(R.string.notification_waiting),
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_PERSISTENT)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setContentIntent(openAppIntent(buildId = null))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    fun updatePersistent(message: String) {
        manager.notify(NOTIFICATION_ID, persistentNotification(message))
    }

    fun showBuildResult(result: BuildResult) {
        val title = context.getString(
            when (result.status) {
                BuildStatus.SUCCESS   -> R.string.notification_build_succeeded
                BuildStatus.FAILED    -> R.string.notification_build_failed
                BuildStatus.CANCELLED -> R.string.notification_build_cancelled
                BuildStatus.STARTED   -> return
            },
            result.projectName,
        )
        val summary = buildString {
            append(formatDuration(result.durationMs))
            if (result.errorCount > 0) {
                append(" · ")
                append(context.resources.getQuantityString(R.plurals.notification_errors, result.errorCount, result.errorCount))
            }
            if (result.warningCount > 0) {
                append(" · ")
                append(context.resources.getQuantityString(R.plurals.notification_warnings, result.warningCount, result.warningCount))
            }
        }
        val preview = result.errors.take(MAX_PREVIEW_ERRORS).joinToString("\n") { it.preview() }

        val notification = NotificationCompat.Builder(context, CHANNEL_BUILD_EVENTS)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (preview.isEmpty()) summary else "$summary\n$preview"),
            )
            .setContentIntent(openAppIntent(buildId = result.buildId))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setWhen(result.finishedAt)
            .setShowWhen(true)
            .setAutoCancel(true)
            .build()

        manager.notify(result.buildId.hashCode(), notification)
    }

    /** Opens [MainActivity]; with [buildId] it navigates straight to that build's log. */
    private fun openAppIntent(buildId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (buildId != null) intent.putExtra(MainActivity.EXTRA_BUILD_ID, buildId)
        return PendingIntent.getActivity(
            context,
            buildId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createChannels() {
        val persistent = NotificationChannel(
            CHANNEL_PERSISTENT,
            context.getString(R.string.notification_channel_connection),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_connection_description)
            setShowBadge(false)
        }

        val buildEvents = NotificationChannel(
            CHANNEL_BUILD_EVENTS,
            context.getString(R.string.notification_channel_builds),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_builds_description)
        }

        manager.createNotificationChannels(listOf(persistent, buildEvents))
    }

    companion object {
        const val CHANNEL_PERSISTENT = "build_notify_persistent"
        const val CHANNEL_BUILD_EVENTS = "build_notify_events"
        const val NOTIFICATION_ID = 1_001
        private const val MAX_PREVIEW_ERRORS = 3
    }
}

private fun BuildIssue.preview(): String {
    val file = filePath?.substringAfterLast('/')
    val location = when {
        file != null && line != null -> "$file:$line "
        file != null                 -> "$file "
        else                         -> ""
    }
    return "• $location$message"
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds.toString().padStart(2, '0')}s" else "${seconds}s"
}
