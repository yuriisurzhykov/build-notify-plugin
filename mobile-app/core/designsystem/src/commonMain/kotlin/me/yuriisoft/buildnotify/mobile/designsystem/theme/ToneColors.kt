package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The colours of one [BuildTone].
 *
 * @property main      icons, dots, status text, accents.
 * @property container pills and tags: [main] at low opacity.
 * @property hero      flat background of the hero panel in this state.
 * @property onMain    content drawn on a solid [main] fill.
 */
@Immutable
data class ToneColors(
    val main: Color,
    val container: Color,
    val hero: Color,
    val onMain: Color,
)
