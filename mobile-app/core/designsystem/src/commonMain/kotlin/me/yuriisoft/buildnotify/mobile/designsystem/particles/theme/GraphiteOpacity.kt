package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.runtime.Immutable

/** Alpha levels, the same in light and dark. Read through [GraphiteTheme.opacity]. */
@Immutable
data class GraphiteOpacity(
    val pressed: Float = 0.85f,
    val disabled: Float = 0.38f,
    /** Inner panel on a hero, so the hero tint shows through. */
    val inset: Float = 0.55f,
    /** Console that sits on a tinted hero. */
    val translucentConsole: Float = 0.6f,
    val logHighlight: Float = 0.08f,
    val divider: Float = 0.08f,
    val progressTrack: Float = 0.1f,
    val dangerOutline: Float = 0.4f,
    /** Secondary text inside a selected or inverse control (counters). */
    val secondaryContent: Float = 0.8f,
    /** Hint text inside a control. */
    val hint: Float = 0.75f,
    /** Resting alpha of a bouncing dot; it reaches 1 at the top of its jump. */
    val restingDot: Float = 0.55f,
    /** Alpha of a pulse ring as it leaves the centre; it fades to 0 at the edge. */
    val pulseRing: Float = 0.35f,
)
