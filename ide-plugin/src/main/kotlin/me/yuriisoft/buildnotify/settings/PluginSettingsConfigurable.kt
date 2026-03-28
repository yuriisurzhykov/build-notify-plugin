package me.yuriisoft.buildnotify.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import me.yuriisoft.buildnotify.BuildNotifyBundle
import me.yuriisoft.buildnotify.network.discovery.MdnsAdvertiser
import me.yuriisoft.buildnotify.network.server.BuildWebSocketServer
import me.yuriisoft.buildnotify.notification.PluginNotifier
import me.yuriisoft.buildnotify.security.CertificateManager
import me.yuriisoft.buildnotify.security.PersistentTrustedClients
import java.awt.Font
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JComponent

/**
 * Settings UI rendered in **Settings → Tools → Build Notifier**.
 *
 * Uses Kotlin UI DSL v2 — built into the IntelliJ Platform, zero extra
 * dependencies, inherits the current IDE Look & Feel automatically.
 *
 * ### Phase 5 change — Trusted Clients panel
 * A new `group` named `settings.group.trusted_clients` is appended after the
 * existing Connection and Notifications groups. It contains:
 *
 * - A dynamically built list of all currently trusted client fingerprints.
 *   Each row displays the fingerprint in monospaced font with a "Revoke" button.
 *   Clicking "Revoke" calls [PersistentTrustedClients.revoke] and re-renders
 *   the panel (via [reset] + [createComponent] cycle triggered by the Configurable
 *   framework when [isModified] returns `true`).
 *
 * - An "Revoke All" emergency button that clears the entire trusted set and the
 *   in-memory pending set in one action. Useful when a device is lost or compromised.
 *
 * ### Rendering strategy
 * The trusted-clients list is rendered at [createComponent] time from a snapshot
 * of [PersistentTrustedClients.trustedFingerprints]. Because Kotlin UI DSL panels
 * are not dynamically re-renderable after creation, revoke actions set a dirty flag
 * that causes [isModified] to return `true`, which prompts the IDE to call [apply]
 * and then re-open the panel — effectively re-rendering the list.
 *
 * This is the standard IntelliJ pattern for settings panels with dynamic content.
 * It is not the most reactive approach, but it avoids the complexity of a custom
 * Swing component while remaining fully functional.
 */
class PluginSettingsConfigurable : Configurable {

    private val settings: PluginSettingsState
        get() = service()

    private val trustedClients: PersistentTrustedClients
        get() = service()

    private var panel: DialogPanel? = null
    private val uiState = PluginSettingsState.State()

    // Tracks whether any Revoke action was taken during this settings session.
    // When true, isModified() returns true, which allows the IDE to call apply()
    // and subsequently re-render the panel with the updated trusted list.
    @Volatile
    private var revokePerformed = false

    override fun getDisplayName(): String =
        BuildNotifyBundle.message("plugin.display.name")

    override fun createComponent(): JComponent {
        loadUiState()
        revokePerformed = false

        return panel {
            // ── Server group ────────────────────────────────────────────────
            group(BuildNotifyBundle.message("settings.group.server")) {
                row(BuildNotifyBundle.message("settings.field.port")) {
                    intTextField(range = 1024..65535)
                        .bindIntText(uiState::port)
                    comment(BuildNotifyBundle.message("settings.field.port.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.service.name")) {
                    textField()
                        .bindText(uiState::serviceName)
                    comment(BuildNotifyBundle.message("settings.field.service.name.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.keystore.path")) {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory
                            .createSingleFileDescriptor()
                            .withTitle(BuildNotifyBundle.message("settings.field.keystore.browse.title")),
                        project = ProjectManager.getInstance().defaultProject,
                        fileChosen = { it.path },
                    )
                        .bindText(uiState::keystorePath)
                        .align(AlignX.FILL)
                        .resizableColumn()
                    comment(BuildNotifyBundle.message("settings.field.keystore.path.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.fingerprint")) {
                    val fp = service<CertificateManager>().fingerprint()
                        ?: BuildNotifyBundle.message("settings.field.fingerprint.unavailable")
                    textField()
                        .applyToComponent {
                            text = fp
                            isEditable = false
                        }
                        .align(AlignX.FILL)
                        .resizableColumn()
                    comment(BuildNotifyBundle.message("settings.field.fingerprint.comment"))
                }
            }

            // ── Connection group ─────────────────────────────────────────────
            group(BuildNotifyBundle.message("settings.group.connection")) {
                row(BuildNotifyBundle.message("settings.field.heartbeat.interval")) {
                    intTextField(range = 5..300)
                        .bindIntText(uiState::heartbeatIntervalSec)
                    comment(BuildNotifyBundle.message("settings.field.heartbeat.interval.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.connection.lost.timeout")) {
                    intTextField(range = 5..300)
                        .bindIntText(uiState::connectionLostTimeoutSec)
                    comment(BuildNotifyBundle.message("settings.field.connection.lost.timeout.comment"))
                }
                row(BuildNotifyBundle.message("settings.field.session.timeout")) {
                    intTextField(range = 1..24 * 60)
                        .bindIntText(uiState::sessionTimeoutMinutes)
                    comment(BuildNotifyBundle.message("settings.field.session.timeout.comment"))
                }
            }

            // ── Notifications group ───────────────────────────────────────────
            group(BuildNotifyBundle.message("settings.group.notifications")) {
                row {
                    checkBox(BuildNotifyBundle.message("settings.field.send.warnings"))
                        .bindSelected(uiState::sendWarnings)
                }
                row(BuildNotifyBundle.message("settings.field.max.issues")) {
                    intTextField(range = 1..100)
                        .bindIntText(uiState::maxIssuesPerNotification)
                    comment(BuildNotifyBundle.message("settings.field.max.issues.comment"))
                }
            }

            // ── Trusted Clients group ──
            group(BuildNotifyBundle.message("settings.group.trusted_clients")) {
                // Description row
                row {
                    comment(BuildNotifyBundle.message("settings.trusted_clients.description"))
                }

                val fingerprints = trustedClients.trustedFingerprints().sorted()

                if (fingerprints.isEmpty()) {
                    // Empty state — shown when no device has been paired yet.
                    row {
                        label(BuildNotifyBundle.message("settings.trusted_clients.empty"))
                            .applyToComponent { foreground = JBColor.GRAY }
                    }
                } else {
                    // One row per trusted fingerprint.
                    fingerprints.forEach { fingerprint ->
                        row {
                            // Display fingerprint in monospaced font for readability.
                            // Groups of two hex digits separated by colons:
                            //   AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:...
                            label(fingerprint)
                                .applyToComponent {
                                    font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
                                    toolTipText = BuildNotifyBundle.message(
                                        "settings.trusted_clients.fingerprint.tooltip"
                                    )
                                }
                                .align(AlignX.FILL)
                                .resizableColumn()

                            button(BuildNotifyBundle.message("settings.trusted_clients.action.revoke")) {
                                trustedClients.revoke(fingerprint)
                                revokePerformed = true
                                // Force the IDE to re-render the panel by signalling a modification.
                                panel?.isModified()
                            }.applyToComponent {
                                toolTipText = BuildNotifyBundle.message(
                                    "settings.trusted_clients.action.revoke.tooltip"
                                )
                            }
                        }
                    }
                }

                // ── Revoke All button ──────────────────────────────────────
                separator()
                row {
                    button(BuildNotifyBundle.message("settings.trusted_clients.action.revoke_all")) {
                        revokeAll()
                    }.applyToComponent {
                        toolTipText = BuildNotifyBundle.message(
                            "settings.trusted_clients.action.revoke_all.tooltip"
                        )
                    }
                    comment(BuildNotifyBundle.message("settings.trusted_clients.action.revoke_all.comment"))
                }
            }
        }.also { panel = it }
    }

    override fun isModified(): Boolean = (panel?.isModified() ?: false) || revokePerformed

    override fun apply() {
        panel?.apply()
        val previous = settings.snapshot()
        settings.loadState(uiState.copy())
        val updated = settings.snapshot()

        val keystoreChanged = previous.keystorePath != updated.keystorePath
        if (keystoreChanged) validateKeystorePath(updated.keystorePath)

        val serverNeedsRestart =
            previous.port != updated.port ||
                    previous.connectionLostTimeoutSec != updated.connectionLostTimeoutSec ||
                    keystoreChanged
        val mdnsNeedsRestart =
            previous.port != updated.port ||
                    previous.serviceName != updated.serviceName

        if (serverNeedsRestart || mdnsNeedsRestart) {
            restartServices(serverNeedsRestart, mdnsNeedsRestart)
        }

        // Reset dirty flag after successful apply.
        revokePerformed = false
    }

    override fun reset() {
        loadUiState()
        revokePerformed = false
        panel?.reset()
    }

    /**
     * Revokes all trusted fingerprints in one action.
     *
     * This is the "emergency" path — use when a device is lost, stolen,
     * or when the user wants to force re-pairing of all devices at once.
     *
     * After revocation the in-memory pending set in [PersistentTrustedClients]
     * is also cleared (handled by [PersistentTrustedClients.revoke] internally).
     */
    private fun revokeAll() {
        trustedClients.trustedFingerprints().forEach { trustedClients.revoke(it) }
        revokePerformed = true
    }

    private fun validateKeystorePath(path: String) {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) return

        val file = Path.of(trimmed)
        val notifier = service<PluginNotifier>()

        if (!Files.exists(file)) {
            notifier.warning(
                BuildNotifyBundle.message("notification.keystore.not.found.title"),
                BuildNotifyBundle.message("notification.keystore.not.found.content", trimmed),
            )
            return
        }

        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            notifier.warning(
                BuildNotifyBundle.message("notification.keystore.unreadable.title"),
                BuildNotifyBundle.message("notification.keystore.unreadable.content", trimmed),
            )
        }
    }

    private fun restartServices(serverNeedsRestart: Boolean, mdnsNeedsRestart: Boolean) {
        val server = service<BuildWebSocketServer>()
        val mdns = service<MdnsAdvertiser>()
        val notifier = service<PluginNotifier>()

        if (serverNeedsRestart) server.stop()
        if (mdnsNeedsRestart) mdns.stop()
        if (serverNeedsRestart) server.start()
        if (mdnsNeedsRestart) mdns.start()

        if (serverNeedsRestart && !server.isActive()) {
            notifier.error(
                BuildNotifyBundle.message("notification.settings.restart.failed.title"),
                BuildNotifyBundle.message("notification.settings.restart.failed.content"),
            )
        }
    }

    private fun loadUiState() {
        val snapshot = settings.snapshot()
        uiState.port = snapshot.port
        uiState.serviceName = snapshot.serviceName
        uiState.sendWarnings = snapshot.sendWarnings
        uiState.maxIssuesPerNotification = snapshot.maxIssuesPerNotification
        uiState.heartbeatIntervalSec = snapshot.heartbeatIntervalSec
        uiState.connectionLostTimeoutSec = snapshot.connectionLostTimeoutSec
        uiState.sessionTimeoutMinutes = snapshot.sessionTimeoutMinutes
        uiState.keystorePath = snapshot.keystorePath
    }
}