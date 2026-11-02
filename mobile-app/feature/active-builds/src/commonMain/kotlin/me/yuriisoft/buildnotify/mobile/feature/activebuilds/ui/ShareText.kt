package me.yuriisoft.buildnotify.mobile.feature.activebuilds.ui

import androidx.compose.runtime.Composable

/**
 * Opens the platform share sheet with plain [text]. This is how a failed build reaches
 * the user's coding agent: Claude, Codex and other agent apps accept shared text.
 */
@Composable
internal expect fun rememberShareText(): (text: String) -> Unit
