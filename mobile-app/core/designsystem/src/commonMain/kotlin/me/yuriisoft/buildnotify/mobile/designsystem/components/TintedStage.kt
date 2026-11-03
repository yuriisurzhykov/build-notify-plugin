package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone

/**
 * Hero-tinted area without padding, for multi-step flows whose steps swap inside it.
 * The tint glides when [tone] changes.
 */
@Composable
fun TintedStage(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.styled(rememberStageStyle(tone)), content = content)
}
