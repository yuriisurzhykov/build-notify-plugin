package me.yuriisoft.buildnotify.mobile.designsystem.molecules.console

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteTextStyle

/**
 * One row of a console rendered as lazy list items, so thousands of log lines stay cheap.
 * Consecutive segments read as one continuous [Console].
 */
@Composable
fun ConsoleSegment(
    position: ConsoleSegmentPosition,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(
        LocalGraphiteContentColor provides GraphiteTheme.colors.consoleContent,
        LocalGraphiteTextStyle provides GraphiteTheme.typography.log,
    ) {
        Box(modifier.fillMaxWidth().styled(rememberConsoleSegmentStyle(position)), content = content)
    }
}
