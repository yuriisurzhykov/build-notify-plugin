package me.yuriisoft.buildnotify.mobile.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import me.yuriisoft.buildnotify.mobile.AppComponent
import me.yuriisoft.buildnotify.mobile.BuildNotifyApp
import me.yuriisoft.buildnotify.mobile.R
import me.yuriisoft.buildnotify.mobile.data.protocol.BuildResultPayload
import me.yuriisoft.buildnotify.mobile.data.protocol.TaskStatus
import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.network.sync.BuildState

/**
 * Foreground service that keeps the WebSocket connection alive and turns build events
 * into notifications while the app is in the background.
 *
 *   - `build.result` → heads-up notification; tapping it opens that build's log.
 *   - Running builds → the ongoing notification shows the project and current task.
 *
 * Lifecycle is driven by [ConnectionServiceManager]: started on
 * [ConnectionState.Connected], stops itself on [ConnectionState.Disconnected].
 *
 * Dependencies are retrieved from the application-scoped [AppComponent][me.yuriisoft.buildnotify.mobile.AppComponent]
 * because Android [Service] instances cannot use constructor injection.
 */
class BuildMonitorService : Service() {

    private lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)

        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ NotificationHelper.NOTIFICATION_ID,
            /* notification = */ notificationHelper.persistentNotification(),
            /* foregroundServiceType = */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING else 0,
        )

        val component = (application as BuildNotifyApp).component
        observeOngoingStatus(component)
        observeBuildResults(component)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun observeOngoingStatus(component: AppComponent) {
        serviceScope.launch {
            combine(
                component.connectionManager.state,
                component.buildStateSync.builds,
            ) { connection, builds ->
                if (connection is ConnectionState.Disconnected) null
                else runningBuildText(builds) ?: connectionText(connection)
            }
                .distinctUntilChanged()
                .collect { text ->
                    if (text == null) stopSelf() else notificationHelper.updatePersistent(text)
                }
        }
    }

    private fun observeBuildResults(component: AppComponent) {
        serviceScope.launch {
            component.activeSession.incoming
                .filterIsInstance<BuildResultPayload>()
                .collect { notificationHelper.showBuildResult(it.result) }
        }
    }

    private fun runningBuildText(builds: Map<String, BuildState>): String? {
        val running = builds.values
            .filterIsInstance<BuildState.Active>()
            .maxByOrNull { it.startedAt }
            ?: return null
        val task = running.tasks.entries.lastOrNull { it.value == TaskStatus.RUNNING }?.key
        return if (task != null) {
            getString(R.string.notification_building_task, running.projectName, task)
        } else {
            getString(R.string.notification_building, running.projectName)
        }
    }

    private fun connectionText(state: ConnectionState): String = when (state) {
        is ConnectionState.Connected       -> getString(R.string.notification_connected, state.host.name)
        is ConnectionState.Connecting      -> getString(R.string.notification_connecting, state.host.name)
        is ConnectionState.PairingRequired -> getString(R.string.notification_pairing, state.host.name)
        is ConnectionState.Reconnecting    -> getString(R.string.notification_reconnecting, state.attempt)
        is ConnectionState.Failed          -> getString(R.string.notification_connection_lost)
        ConnectionState.Disconnected,
        ConnectionState.Idle               -> getString(R.string.notification_waiting)
    }
}
