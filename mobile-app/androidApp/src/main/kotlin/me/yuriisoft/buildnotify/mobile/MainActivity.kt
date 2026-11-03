package me.yuriisoft.buildnotify.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import me.yuriisoft.buildnotify.mobile.core.navigation.routes.BuildLogsDestination
import me.yuriisoft.buildnotify.mobile.service.ConnectionServiceManager

class MainActivity : ComponentActivity() {

    /** Consumed exactly once by the navigation graph; survives until collected. */
    private val externalRoutes = Channel<String>(Channel.BUFFERED)

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val component = (application as BuildNotifyApp).component

        ConnectionServiceManager(this, component.connectionManager)
            .bind(lifecycleScope)

        // A recreated activity (rotation, process restore) still carries the original
        // intent; it was already handled, so only route on a fresh start.
        if (savedInstanceState == null) routeFrom(intent)
        requestNotificationPermission()

        setContent {
            App(
                screens = component.screens,
                startRoute = component.startRoute,
                connectionState = component.connectionManager.state,
                externalRoutes = externalRoutes.receiveAsFlow(),
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFrom(intent)
    }

    private fun routeFrom(intent: Intent?) {
        val buildId = intent?.getStringExtra(EXTRA_BUILD_ID) ?: return
        intent.removeExtra(EXTRA_BUILD_ID)
        externalRoutes.trySend(BuildLogsDestination.routeFor(buildId))
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        const val EXTRA_BUILD_ID = "me.yuriisoft.buildnotify.extra.BUILD_ID"
    }
}
