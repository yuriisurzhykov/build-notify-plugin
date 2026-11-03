package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_hand_off
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_all_built
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_colors
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_today
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_typography
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_status_success
import me.yuriisoft.buildnotify.mobile.designsystem.particles.text.TextValue
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ColorsSection() {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    Section(stringResource(Res.string.catalog_section_colors)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
            verticalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            Swatch("background", colors.background)
            Swatch("surface", colors.surface)
            Swatch("raised", colors.surfaceRaised)
            Swatch("console", colors.console)
            Swatch("running", colors.running.main)
            Swatch("success", colors.success.main)
            Swatch("failure", colors.failure.main)
            Swatch("warning", colors.warning.main)
        }
    }
}

@Composable
internal fun TypographySection() {
    val type = GraphiteTheme.typography
    Section(stringResource(Res.string.catalog_section_typography)) {
        TypeSample("display", type.display, stringResource(Res.string.catalog_all_built))
        TypeSample("headline", type.headline, stringResource(Res.string.catalog_action_hand_off))
        TypeSample("title", type.title, CatalogSamples.SHOP_ANDROID)
        TypeSample("body", type.body, CatalogSamples.ERROR_UNRESOLVED)
        TypeSample(
            "meta",
            type.meta,
            TextValue.join(
                listOf(TextValue.of(Res.string.catalog_status_success), TextValue.of(CatalogSamples.DURATION_SUCCESS)),
                separator = TextValue.DotSeparator,
            ).string(),
        )
        TypeSample("label", type.label, stringResource(Res.string.catalog_section_today))
        TypeSample("timer", type.timer, CatalogSamples.ELAPSED)
        TypeSample("mono", type.mono, CatalogSamples.TASK_COMPILE_APP)
        TypeSample("log", type.log, CatalogSamples.LOG_UNUSED)
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    val catalog = GraphiteTheme.dimens.catalog
    Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.inline)) {
        Box(
            Modifier
                .size(width = catalog.swatchWidth, height = catalog.swatchHeight)
                .background(color, GraphiteTheme.shapes.control)
                .border(GraphiteTheme.sizes.hairline, GraphiteTheme.colors.line, GraphiteTheme.shapes.control),
        )
        Text(name, style = GraphiteTheme.typography.caption, color = GraphiteTheme.colors.contentSecondary)
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle, sample: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.l)) {
        Text(
            name,
            modifier = Modifier.width(GraphiteTheme.dimens.catalog.sampleLabelColumn),
            style = GraphiteTheme.typography.log,
            color = GraphiteTheme.colors.contentTertiary,
        )
        Text(sample, style = style, maxLines = 1)
    }
}
