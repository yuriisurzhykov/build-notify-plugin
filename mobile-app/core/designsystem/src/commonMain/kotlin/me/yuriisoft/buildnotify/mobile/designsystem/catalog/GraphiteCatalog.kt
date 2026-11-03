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
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_intro
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.templates.screens.ScreenSurface
import org.jetbrains.compose.resources.stringResource

/**
 * Living reference of every Graphite token and component, in the current theme.
 * Host it on any platform inside [GraphiteTheme][me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme]
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
                Text(CatalogSamples.PRODUCT_NAME, style = type.display)
                Text(
                    stringResource(Res.string.catalog_intro),
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
