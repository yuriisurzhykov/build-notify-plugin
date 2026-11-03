package me.yuriisoft.buildnotify.mobile.designsystem.atoms.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = GraphiteTheme.colors.line,
    thickness: Dp = GraphiteTheme.sizes.hairline,
) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color),
    )
}
