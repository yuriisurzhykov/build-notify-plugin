package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Resolved look of a [Button] variant: its container Style plus the content colour. */
@Immutable
internal data class ButtonAppearance(
    val style: Style,
    val content: Color,
)
