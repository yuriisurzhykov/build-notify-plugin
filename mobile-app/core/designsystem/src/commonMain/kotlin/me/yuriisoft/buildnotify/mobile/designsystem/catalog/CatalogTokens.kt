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
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.components.Text
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

@Composable
internal fun ColorsSection() {
    val spacing = GraphiteTheme.spacing
    val colors = GraphiteTheme.colors
    Section("Цвет") {
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
    Section("Типографика") {
        TypeSample("display", type.display, "Всё собрано")
        TypeSample("headline", type.headline, "Передать агенту")
        TypeSample("title", type.title, "shop-android")
        TypeSample("body", type.body, "Unresolved reference: PaymentProcessor")
        TypeSample("meta", type.meta, "Успешно · 1m 45s")
        TypeSample("label", type.label, "Сегодня")
        TypeSample("timer", type.timer, "1:12")
        TypeSample("mono", type.mono, ":app:compileDebugKotlin")
        TypeSample("log", type.log, "w: Parameter 'context' is never used")
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(width = 72.dp, height = 48.dp)
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
            modifier = Modifier.width(72.dp),
            style = GraphiteTheme.typography.log,
            color = GraphiteTheme.colors.contentTertiary,
        )
        Text(sample, style = style, maxLines = 1)
    }
}
