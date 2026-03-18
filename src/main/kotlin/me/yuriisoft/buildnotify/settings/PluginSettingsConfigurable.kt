package me.yuriisoft.buildnotify.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import me.yuriisoft.buildnotify.BuildNotifyBundle
import javax.swing.JComponent

/**
 * Settings UI rendered in Settings → Tools → Build Notify.
 *
 * Uses Kotlin UI DSL v2 — built into the IntelliJ Platform, zero extra dependencies,
 * inherits the current IDE Look & Feel (Light / Darcula / High Contrast) automatically.
 *
 * Jewel/Compose was intentionally dropped: it requires complex bundledModule wiring
 * for AS builds and is not officially supported for third-party plugins as of 2025.
 */
class PluginSettingsConfigurable : Configurable {

    private val settings: PluginSettings get() = service()
    private var panel: DialogPanel? = null

    override fun getDisplayName(): String =
        BuildNotifyBundle.message("plugin.display.name")

    override fun createComponent(): JComponent {
        val state = settings.state
        return panel {
            group(BuildNotifyBundle.message("settings.group.server")) {
                row(BuildNotifyBundle.message("settings.field.port")) {
                    intTextField(range = 1024..65535)
                        .bindIntText(state::port)
                    comment(BuildNotifyBundle.message("settings.field.port.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.service.name")) {
                    textField()
                        .bindText(state::serviceName)
                    comment(BuildNotifyBundle.message("settings.field.service.name.comment"))
                }
            }
            group(BuildNotifyBundle.message("settings.group.connection")) {
                row(BuildNotifyBundle.message("settings.field.heartbeat.interval")) {
                    intTextField(range = 5..300)
                        .bindIntText(state::heartbeatIntervalSec)
                    comment(BuildNotifyBundle.message("settings.field.heartbeat.interval.comment"))
                }
            }
            group(BuildNotifyBundle.message("settings.group.notifications")) {
                row {
                    checkBox(BuildNotifyBundle.message("settings.field.send.warnings"))
                        .bindSelected(state::sendWarnings)
                }
                row(BuildNotifyBundle.message("settings.field.max.issues")) {
                    intTextField(range = 1..100)
                        .bindIntText(state::maxIssuesPerNotification)
                    comment(BuildNotifyBundle.message("settings.field.max.issues.comment"))
                }
            }
        }.also { panel = it }
    }

    override fun isModified(): Boolean = panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }
}