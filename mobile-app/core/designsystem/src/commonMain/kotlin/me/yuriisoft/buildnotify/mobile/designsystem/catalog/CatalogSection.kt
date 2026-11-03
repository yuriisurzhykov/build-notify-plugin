package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.texts.SectionHeader
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme

@Composable
internal fun Section(
    title: String,
    padded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = GraphiteTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
        SectionHeader(title, Modifier.padding(horizontal = spacing.screen))
        Column(
            modifier = if (padded) Modifier.padding(horizontal = spacing.screen) else Modifier,
            verticalArrangement = Arrangement.spacedBy(spacing.m),
            content = content,
        )
    }
}
