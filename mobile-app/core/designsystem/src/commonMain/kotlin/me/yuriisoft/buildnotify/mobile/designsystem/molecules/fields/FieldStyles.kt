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
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/** Outline pill that fills with the inverse colour when selected. */
@Composable
internal fun rememberChipStyle(): Style {
    val colors = GraphiteTheme.colors
    val pill = GraphiteTheme.shapes.pill
    val sizes = GraphiteTheme.sizes
    val field = GraphiteTheme.dimens.field
    val motion = GraphiteTheme.motion
    return remember(colors, pill, sizes, field, motion) {
        Style {
            shape(pill)
            minHeight(sizes.chip)
            contentPaddingHorizontal(field.chipPaddingHorizontal)
            background(Color.Transparent)
            border(sizes.hairline, colors.line)
            selected {
                animate(motion.fast()) {
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
    val field = GraphiteTheme.dimens.field
    val motion = GraphiteTheme.motion
    return remember(colors, control, sizes, field, motion) {
        Style {
            shape(control)
            contentPadding(field.choicePadding)
            background(Color.Transparent)
            border(sizes.hairline, colors.line)
            selected {
                animate(motion.fast()) {
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
    val field = GraphiteTheme.dimens.field
    val motion = GraphiteTheme.motion
    return remember(colors, pill, field, motion) {
        SwitchStyles(
            track = Style {
                size(field.switchWidth, field.switchHeight)
                shape(pill)
                contentPadding(field.switchInset)
                background(colors.lineStrong)
                checked {
                    animate(motion.slow()) { background(colors.success.main) }
                }
            },
            thumb = Style {
                size(field.switchThumb)
                shape(pill)
                background(colors.contentSecondary)
                checked {
                    animate(motion.settle()) {
                        translation(field.switchThumbTravel.toPx(), 0f)
                        background(colors.success.onMain)
                    }
                }
            },
        )
    }
}
