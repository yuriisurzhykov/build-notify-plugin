package me.yuriisoft.buildnotify.mobile.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * "is everything OK right now". Its tint follows [tone] and glides between tones.
 */
@Composable
fun HeroPanel(
    tone: BuildTone,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.fillMaxWidth().styled(rememberHeroStyle(tone)),
            verticalArrangement = Arrangement.spacedBy(HeroGap),
            content = content,
        )
    }
}

/** Bordered surface for secondary groups. Tappable when [onClick] is set. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val style = rememberCardStyle()
    val surface = if (onClick != null) Modifier.styledClickable(style, onClick) else Modifier.styled(style)
    CompositionLocalProvider(LocalGraphiteContentColor provides GraphiteTheme.colors.content) {
        Column(
            modifier = modifier.then(surface),
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
        modifier = modifier.fillMaxWidth().styled(rememberInsetStyle()),
        verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs),
        content = content,
    )
}

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

private val HeroGap = 18.dp
private val SheetGap = 18.dp
