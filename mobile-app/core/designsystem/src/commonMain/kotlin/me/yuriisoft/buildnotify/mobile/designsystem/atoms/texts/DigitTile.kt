package me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

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

@Composable
internal fun rememberDigitTileStyle(): Style {
    val colors = GraphiteTheme.colors
    val surfaceShape = GraphiteTheme.shapes.control
    val tile = GraphiteTheme.dimens.tile
    return remember(colors, surfaceShape, tile) {
        Style {
            size(tile.digitWidth, tile.digitHeight)
            shape(surfaceShape)
            background(colors.surfaceRaised)
        }
    }
}
