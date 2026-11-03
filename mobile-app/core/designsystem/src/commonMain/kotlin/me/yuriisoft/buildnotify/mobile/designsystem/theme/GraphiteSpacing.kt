package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4 pt spacing scale plus the two layout constants every screen uses. */
@Immutable
data class GraphiteSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    /** Horizontal padding of screen content. */
    val screen: Dp = 20.dp,
    /** Inset of the hero panel from the screen edge. */
    val heroInset: Dp = 8.dp,
    /** Vertical gap between major sections. */
    val section: Dp = 28.dp,
)
