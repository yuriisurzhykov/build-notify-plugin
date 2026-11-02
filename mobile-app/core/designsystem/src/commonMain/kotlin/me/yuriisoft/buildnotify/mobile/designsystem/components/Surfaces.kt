package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteContentColor
import me.yuriisoft.buildnotify.mobile.designsystem.theme.LocalGraphiteTextStyle

/** Screen background. Put it at the root of each screen. */
@Composable
fun ScreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.background(GraphiteTheme.colors.background),
            content = content,
        )
    }
}

/**
 * The hero: one flat panel at the top of the screen that answers
 * "is everything OK right now". Its background is the [tone]'s hero tint.
 */
@Composable
fun HeroPanel(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GraphiteTheme.colors
    CompositionLocalProvider(LocalGraphiteContentColor provides colors.content) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = GraphiteTheme.spacing.heroInset)
                .clip(GraphiteTheme.shapes.hero)
                .background(colors.tone(tone).hero)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}

/** Bordered surface for secondary groups. Tappable when [onClick] is set. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(GraphiteTheme.spacing.l),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GraphiteTheme.colors
    val shape = GraphiteTheme.shapes.card
    val click = if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier
    CompositionLocalProvider(LocalGraphiteContentColor provides colors.content) {
        Column(
            modifier = modifier
                .then(click)
                .clip(shape)
                .background(colors.surface)
                .border(GraphiteTheme.sizes.hairline, colors.line, shape)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.m),
            content = content,
        )
    }
}

/**
 * Inner panel on a hero for the key detail (first error, prompt preview).
 * Translucent so the hero tint shows through.
 */
@Composable
fun InsetPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GraphiteTheme.shapes.inset)
            .background(GraphiteTheme.colors.console.copy(alpha = 0.55f))
            .padding(horizontal = GraphiteTheme.spacing.l, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs),
        content = content,
    )
}

/** Modal bottom sheet body with a drag handle. The host decides how it is presented. */
@Composable
fun SheetSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = GraphiteTheme.colors
    CompositionLocalProvider(LocalGraphiteContentColor provides colors.content) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(GraphiteTheme.shapes.sheet)
                .background(colors.surface)
                .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 4.dp)
                    .background(colors.lineStrong, GraphiteTheme.shapes.pill),
            )
            content()
        }
    }
}

/** Section title above a list: "Сегодня", "Запустить снова". */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(bottom = 6.dp),
        style = GraphiteTheme.typography.label,
        color = GraphiteTheme.colors.contentTertiary,
    )
}

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

/** Horizontal gap helper for rows. */
@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

/** Vertical gap helper for columns. */
@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))

/** Provides [LocalGraphiteTextStyle] for a subtree, e.g. mono for a block of build output. */
@Composable
fun ProvideTextStyle(
    style: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteTextStyle provides style, content = content)
}
