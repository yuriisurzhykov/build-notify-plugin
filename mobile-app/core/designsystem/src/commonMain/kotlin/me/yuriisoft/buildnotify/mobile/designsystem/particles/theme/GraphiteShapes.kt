package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

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
