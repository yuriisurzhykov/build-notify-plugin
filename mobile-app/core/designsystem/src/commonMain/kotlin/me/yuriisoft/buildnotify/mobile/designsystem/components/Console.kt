package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteContentColor
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteTextStyle

/**
 * Dark mono block for build output. Stays dark in the light theme too.
 * [translucent] lets a hero tint show through (live tail inside a hero).
 */
@Composable
fun Console(
    modifier: Modifier = Modifier,
    translucent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GraphiteTheme.colors
    CompositionLocalProvider(
        LocalGraphiteContentColor provides colors.consoleContent,
        LocalGraphiteTextStyle provides GraphiteTheme.typography.log,
    ) {
        Column(
            modifier = modifier.fillMaxWidth().styled(rememberConsoleStyle(translucent)),
            verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xxs),
            content = content,
        )
    }
}

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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (offset != null) {
            Text(
                offset,
                modifier = Modifier.width(52.dp),
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

/** Compiler diagnostic: message plus a mono `File.kt:42` location. */
@Composable
fun IssueText(
    message: String,
    location: String?,
    modifier: Modifier = Modifier,
    locationColor: androidx.compose.ui.graphics.Color = GraphiteTheme.colors.failure.main,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xxs)) {
        Text(message, style = GraphiteTheme.typography.body)
        if (location != null) Text(location, style = GraphiteTheme.typography.log, color = locationColor)
    }
}
