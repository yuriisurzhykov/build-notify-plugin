package me.yuriisoft.buildnotify.network.server.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import me.yuriisoft.buildnotify.BuildNotifyBundle
import me.yuriisoft.buildnotify.security.PersistentTrustedClients
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

/**
 * Non-modal IDE dialog shown when an unknown client certificate is presented
 * during a mutual-TLS handshake.
 *
 * ## Design decisions
 *
 * - `isModal = false` — the user must never be blocked from IDE work while
 *   a mobile device is waiting for approval. The dialog floats over the IDE.
 * - Two explicit actions: **Trust** and **Reject**. Closing the window without
 *   choosing is equivalent to "decide later" — the fingerprint remains pending
 *   and the mobile client will keep retrying via `ExponentialBackoff`.
 * - The fingerprint is rendered in monospace font, formatted in groups of two
 *   hex digits separated by colons, matching the display on the mobile side.
 *
 * ## Threading
 *
 * Must be constructed and shown on the EDT.
 * [ClientToFuTrustManager.onFirstSeen] guarantees this via `invokeLater`.
 *
 * @param clientFingerprint SHA-256 fingerprint, format `AB:CD:EF:...`
 */
class ClientApprovalDialog(
    private val clientFingerprint: String,
) : DialogWrapper(/* project = */ null, /* canBeParent = */ true) {

    private val logger = thisLogger()
    private val store: PersistentTrustedClients get() = service()

    init {
        title = BuildNotifyBundle.message("dialog.client.approval.title")
        isModal = false
        init()
    }

    // ── DialogWrapper overrides ───────────────────────────────────────────────

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            weightx = 1.0
            gridx = 0
        }

        // Explanatory label
        gbc.gridy = 0
        gbc.insets = JBUI.insetsBottom(8)
        panel.add(
            JBLabel(BuildNotifyBundle.message("dialog.client.approval.description")),
            gbc,
        )

        // Fingerprint display — read-only, monospaced, selectable
        gbc.gridy = 1
        gbc.insets = JBUI.insetsBottom(4)
        panel.add(
            JBLabel(BuildNotifyBundle.message("dialog.client.approval.fingerprint.label")),
            gbc,
        )

        gbc.gridy = 2
        gbc.insets = JBUI.emptyInsets()
        panel.add(fingerprintArea(), gbc)

        return panel
    }

    /**
     * Replaces the default OK/Cancel pair with **Trust** and **Reject**.
     *
     * [createActions] must return the actions in left-to-right display order.
     * "Reject" is intentionally on the right to reduce accidental clicks.
     */
    override fun createActions(): Array<Action> = arrayOf(trustAction(), rejectAction())

    // We do not want a default "Cancel" button — the user can just close the window
    // to defer the decision. Overriding to suppress it.
    override fun createCancelAction(): Action? = null

    private fun trustAction(): Action = dialogAction(
        text = BuildNotifyBundle.message("dialog.client.approval.action.trust"),
    ) {
        store.trust(clientFingerprint)
        logger.info("Client certificate trusted: $clientFingerprint")
        close(OK_EXIT_CODE)
    }

    private fun rejectAction(): Action = dialogAction(
        text = BuildNotifyBundle.message("dialog.client.approval.action.reject"),
    ) {
        store.reject(clientFingerprint)
        logger.info("Client certificate rejected: $clientFingerprint")
        close(CANCEL_EXIT_CODE)
    }

    /**
     * A read-only, selectable text area showing the fingerprint in a monospaced
     * font. [JTextArea] is used instead of [JBLabel] so the user can select and
     * copy the fingerprint for out-of-band verification.
     */
    private fun fingerprintArea(): JTextArea = JTextArea(clientFingerprint).apply {
        isEditable = false
        isOpaque = false
        lineWrap = true
        wrapStyleWord = false
        font = Font(Font.MONOSPACED, Font.BOLD, 12)
        border = JBUI.Borders.empty(4)
    }

    /**
     * Minimal inline factory to avoid a separate anonymous class per action.
     * Keeps action construction readable without a heavyweight DSL.
     */
    private fun dialogAction(text: String, perform: () -> Unit): Action =
        object : DialogWrapperAction(text) {
            override fun doAction(e: java.awt.event.ActionEvent?) = perform()
        }
}