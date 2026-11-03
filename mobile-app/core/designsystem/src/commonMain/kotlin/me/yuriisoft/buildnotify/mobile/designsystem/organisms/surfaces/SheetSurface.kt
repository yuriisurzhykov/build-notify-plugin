package me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.particles.style.styled
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.LocalGraphiteContentColor

/** Modal bottom sheet body with a drag handle; present it with [ModalSheetHost]. */
@Composable
fun SheetSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.fillMaxWidth().styled(rememberSheetStyle()),
            verticalArrangement = Arrangement.spacedBy(SheetGap),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).styled(rememberSheetHandleStyle()))
            content()
        }
    }
}

private val SheetGap = 18.dp
