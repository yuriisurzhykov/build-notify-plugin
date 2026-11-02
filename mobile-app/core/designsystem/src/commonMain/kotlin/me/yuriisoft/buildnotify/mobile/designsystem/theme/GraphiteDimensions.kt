package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
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

@Immutable
data class GraphiteShapes(
    /** Highlighted log line. */
    val log: Shape = RoundedCornerShape(6.dp),
    /** Tags ("агент"). */
    val tag: Shape = RoundedCornerShape(6.dp),
    /** Buttons and inputs. */
    val control: Shape = RoundedCornerShape(14.dp),
    /** Console blocks and inner panels. */
    val inset: Shape = RoundedCornerShape(16.dp),
    /** Cards. */
    val card: Shape = RoundedCornerShape(18.dp),
    /** The hero status panel. */
    val hero: Shape = RoundedCornerShape(28.dp),
    /** Bottom sheets: top corners only. */
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    /** Pills, chips, dots, round icon buttons. */
    val pill: Shape = CircleShape,
)

@Immutable
data class GraphiteSizes(
    /** Minimum touch target for anything tappable. */
    val touchTarget: Dp = 44.dp,
    val buttonRegular: Dp = 48.dp,
    val buttonLarge: Dp = 54.dp,
    val chip: Dp = 36.dp,
    val pill: Dp = 24.dp,
    val listRow: Dp = 52.dp,
    val statusDot: Dp = 8.dp,
    val iconSmall: Dp = 14.dp,
    val icon: Dp = 18.dp,
    val iconLarge: Dp = 22.dp,
    val progressTrack: Dp = 3.dp,
    val hairline: Dp = 1.dp,
)
