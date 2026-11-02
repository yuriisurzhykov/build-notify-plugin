package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteContentColor
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteTextStyle

/**
 * Graphite text. Colour resolves as: [color] → [style]'s colour → [LocalGraphiteContentColor].
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalGraphiteTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.resolve(color, textAlign),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalGraphiteTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.resolve(color, textAlign),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

@Composable
private fun TextStyle.resolve(color: Color, textAlign: TextAlign?): TextStyle {
    val resolved = color.takeOrElse { this.color.takeOrElse { LocalGraphiteContentColor.current } }
    val withColor = copy(color = resolved)
    return if (textAlign != null) withColor.copy(textAlign = textAlign) else withColor
}
