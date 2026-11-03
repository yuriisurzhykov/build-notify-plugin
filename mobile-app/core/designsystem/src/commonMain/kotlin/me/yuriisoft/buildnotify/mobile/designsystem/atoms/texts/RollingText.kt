package me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.Swap
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.roll

/**
 * Text whose characters roll like a split-flap board: only the characters that changed
 * move. Characters are keyed by their position from the end, so the seconds keep rolling
 * in place when "9:59" becomes "10:00".
 */
@Composable
fun RollingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        text.forEachIndexed { index, char ->
            key(text.length - index) {
                Swap(targetState = char, transition = { roll() }, label = "rollingChar") { value ->
                    Text(value.toString(), style = style)
                }
            }
        }
    }
}
