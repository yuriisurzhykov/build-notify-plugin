package me.yuriisoft.buildnotify.mobile.designsystem.organisms.console

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.IssueText
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

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

@Composable
internal fun rememberIssueCardStyle(tone: BuildTone): Style {
    val status = GraphiteTheme.status(tone)
    val surfaceShape = GraphiteTheme.shapes.control
    val padding = GraphiteTheme.spacing.m
    return remember(status, surfaceShape, padding) {
        Style {
            shape(surfaceShape)
            background(status.container)
            contentPadding(padding)
        }
    }
}
