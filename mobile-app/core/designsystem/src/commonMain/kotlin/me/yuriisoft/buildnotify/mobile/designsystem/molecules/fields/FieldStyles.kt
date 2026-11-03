package me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields

import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.border
import androidx.compose.foundation.style.checked
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.selected
import androidx.compose.foundation.style.size
import androidx.compose.foundation.style.translation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.motion.GraphiteMotion
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Outline pill that fills with the inverse colour when selected. */
@Composable
internal fun rememberChipStyle(): Style {
    val colors = GraphiteTheme.colors
    val pill = GraphiteTheme.shapes.pill
    val sizes = GraphiteTheme.sizes
    return remember(colors, pill, sizes) {
        Style {
            shape(pill)
            minHeight(sizes.chip)
            contentPaddingHorizontal(ChipPadding)
            background(Color.Transparent)
            border(sizes.hairline, colors.line)
            selected {
                animate(GraphiteMotion.fast()) {
                    background(colors.inverse)
                    borderColor(colors.inverse)
                }
            }
        }
    }
}

/** Same selection language as the chip, as a card with a title and a hint. */
@Composable
internal fun rememberChoiceCardStyle(): Style {
    val colors = GraphiteTheme.colors
    val control = GraphiteTheme.shapes.control
    val sizes = GraphiteTheme.sizes
    return remember(colors, control, sizes) {
        Style {
            shape(control)
            contentPadding(ChoicePadding)
            background(Color.Transparent)
            border(sizes.hairline, colors.line)
            selected {
                animate(GraphiteMotion.fast()) {
                    background(colors.inverse)
                    borderColor(colors.inverse)
                }
            }
        }
    }
}

/** Track turns green and the thumb slides over when checked; both animate in the Style. */
@Composable
internal fun rememberSwitchStyles(): SwitchStyles {
    val colors = GraphiteTheme.colors
    val pill = GraphiteTheme.shapes.pill
    return remember(colors, pill) {
        SwitchStyles(
            track = Style {
                size(SwitchWidth, SwitchHeight)
                shape(pill)
                contentPadding(SwitchInset)
                background(colors.lineStrong)
                checked {
                    animate(GraphiteMotion.slow()) { background(colors.success.main) }
                }
            },
            thumb = Style {
                size(ThumbSize)
                shape(pill)
                background(colors.contentSecondary)
                checked {
                    animate(GraphiteMotion.settle()) {
                        translation(ThumbTravel.toPx(), 0f)
                        background(colors.success.onMain)
                    }
                }
            },
        )
    }
}

private val ChipPadding = 14.dp

private val ChoicePadding = 10.dp

private val SwitchWidth = 46.dp

private val SwitchHeight = 28.dp

private val SwitchInset = 3.dp

private val ThumbSize = 22.dp

private val ThumbTravel = SwitchWidth - ThumbSize - SwitchInset * 2
