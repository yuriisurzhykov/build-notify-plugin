package me.yuriisoft.buildnotify.mobile.designsystem.molecules.console

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

/**
 * One log line, meant for a [Console].
 *
 * @param offset   gutter text such as `+12.4s`; `null` hides the gutter.
 * @param singleLine ellipsize instead of wrapping (compact live tail).
 */
@Composable
fun LogLine(
    text: String,
    modifier: Modifier = Modifier,
    tone: LogTone = LogTone.Default,
    offset: String? = null,
    singleLine: Boolean = false,
) {
    val colors = GraphiteTheme.colors
    val color = when (tone) {
        LogTone.Muted   -> colors.consoleMuted
        LogTone.Default -> colors.consoleContent
        LogTone.Warning -> colors.consoleWarning
        LogTone.Error   -> colors.consoleError
    }
    Row(
        modifier = modifier.fillMaxWidth().styled(rememberLogLineStyle(highlighted = tone == LogTone.Error)),
        horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.group),
    ) {
        if (offset != null) {
            Text(
                offset,
                modifier = Modifier.width(GraphiteTheme.dimens.console.offsetColumn),
                color = colors.consoleGutter,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = color,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
        )
    }
}
