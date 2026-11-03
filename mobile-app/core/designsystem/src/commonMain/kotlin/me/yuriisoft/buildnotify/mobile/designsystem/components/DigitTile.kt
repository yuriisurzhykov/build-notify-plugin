package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** One digit of a pairing PIN on a raised tile. */
@Composable
fun DigitTile(
    digit: Char,
    modifier: Modifier = Modifier,
) {
    Box(modifier.styled(rememberDigitTileStyle()), contentAlignment = Alignment.Center) {
        Text(digit.toString(), style = GraphiteTheme.typography.pin)
    }
}
