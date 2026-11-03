package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/** A compiler diagnostic on a card tinted by [tone] (failure for errors, warning for warnings). */
@Composable
fun IssueCard(
    message: String,
    location: String?,
    tone: BuildTone,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().styled(rememberIssueCardStyle(tone))) {
        IssueText(message, location, locationColor = GraphiteTheme.status(tone).main)
    }
}
