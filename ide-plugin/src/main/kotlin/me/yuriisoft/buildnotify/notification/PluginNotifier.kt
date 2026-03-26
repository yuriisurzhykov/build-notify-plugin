package me.yuriisoft.buildnotify.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
class PluginNotifier {

    private val group
        get() = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)

    fun info(title: String, content: String) =
        notify(title, content, NotificationType.INFORMATION)

    fun warning(title: String, content: String) =
        notify(title, content, NotificationType.WARNING)

    fun error(title: String, content: String) =
        notify(title, content, NotificationType.ERROR)

    private fun notify(title: String, content: String, type: NotificationType) {
        group.createNotification(title, content, type).notify(null)
    }

    companion object {
        private const val GROUP_ID = "BuildNotify"

        @JvmStatic
        fun getInstance(): PluginNotifier = service()
    }
}
