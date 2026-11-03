package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.components.ScreenSurface
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

/**
 * Living reference of every Graphite token and component, in the current theme.
 * Host it on any platform inside [GraphiteTheme][me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme]
 * (e.g. a desktop `Window { GraphiteTheme { GraphiteCatalog() } }`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GraphiteCatalog(modifier: Modifier = Modifier) {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    val type = GraphiteTheme.typography

    ScreenSurface(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(spacing.section),
        ) {
            Column(
                Modifier.padding(horizontal = spacing.screen),
                verticalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                Text("Graphite", style = type.display)
                Text(
                    "Тихий интерфейс, громкие сигналы. Цвет появляется только там, где есть статус.",
                    style = type.bodySmall,
                    color = colors.contentSecondary,
                )
            }

            ColorsSection()

            TypographySection()

            StatusSection()

            ButtonsSection()

            FailedHeroSection()

            RunningHeroSection()

            HistorySection()

            LogSection()

            SheetSection()
        }
    }
}



