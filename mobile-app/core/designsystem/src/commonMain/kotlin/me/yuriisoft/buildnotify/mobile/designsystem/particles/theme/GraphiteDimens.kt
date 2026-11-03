package me.yuriisoft.buildnotify.mobile.designsystem.particles.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Component metrics: the fixed sizes and paddings of Graphite components, grouped by
 * component family. Generic gaps live in [GraphiteSpacing], generic sizes in [GraphiteSizes].
 * Read through [GraphiteTheme.dimens]; components never declare their own `dp` values.
 */
@Immutable
data class GraphiteDimens(
    val status: Status = Status(),
    val field: Field = Field(),
    val surface: Surface = Surface(),
    val console: Console = Console(),
    val list: ListRow = ListRow(),
    val tile: Tile = Tile(),
    val visual: Visual = Visual(),
    val layout: Layout = Layout(),
    val catalog: Catalog = Catalog(),
) {
    @Immutable
    data class Status(
        val pillGlyph: Dp = 12.dp,
        val pillDot: Dp = 6.dp,
        val pillPaddingStart: Dp = 8.dp,
        val pillPaddingEnd: Dp = 10.dp,
        val tagPaddingHorizontal: Dp = 6.dp,
        val tagPaddingVertical: Dp = 1.dp,
        val badgeDot: Dp = 12.dp,
        val connectionDot: Dp = 7.dp,
        val iconBadge: Badge = Badge(diameter = 76.dp, icon = 32.dp),
        val iconBadgeSmall: Badge = Badge(diameter = 52.dp, icon = 26.dp),
    )

    @Immutable
    data class Badge(val diameter: Dp, val icon: Dp)

    @Immutable
    data class Field(
        val chipPaddingHorizontal: Dp = 14.dp,
        val choicePadding: Dp = 10.dp,
        val switchWidth: Dp = 46.dp,
        val switchHeight: Dp = 28.dp,
        val switchInset: Dp = 3.dp,
        val switchThumb: Dp = 22.dp,
    ) {
        /** How far the thumb travels between off and on. */
        val switchThumbTravel: Dp get() = switchWidth - switchThumb - switchInset * 2
    }

    @Immutable
    data class Surface(
        val heroPaddingHorizontal: Dp = 20.dp,
        val heroPaddingTop: Dp = 24.dp,
        val heroPaddingBottom: Dp = 20.dp,
        val heroGap: Dp = 18.dp,
        val insetPaddingVertical: Dp = 14.dp,
        val sheetPaddingHorizontal: Dp = 20.dp,
        val sheetPaddingTop: Dp = 10.dp,
        val sheetPaddingBottom: Dp = 28.dp,
        val sheetGap: Dp = 18.dp,
        val sheetHandleWidth: Dp = 36.dp,
        val sheetHandleHeight: Dp = 4.dp,
        val sectionHeaderBottom: Dp = 6.dp,
    )

    @Immutable
    data class Console(
        val linePaddingHorizontal: Dp = 6.dp,
        val linePaddingVertical: Dp = 3.dp,
        val cornerRadius: Dp = 16.dp,
        val edgePadding: Dp = 6.dp,
        val offsetColumn: Dp = 52.dp,
        val previewMaxHeight: Dp = 220.dp,
    )

    @Immutable
    data class ListRow(
        val timeColumn: Dp = 40.dp,
    )

    @Immutable
    data class Tile(
        val digitWidth: Dp = 40.dp,
        val digitHeight: Dp = 52.dp,
    )

    @Immutable
    data class Visual(
        /** The animated stage of the connection flow (pulse rings and their badge). */
        val stage: Dp = 168.dp,
        val ringStroke: Dp = 1.5.dp,
        val bouncingDot: Dp = 6.dp,
    )

    @Immutable
    data class Layout(
        /** Space kept free under a list so the floating button never covers its last item. */
        val floatingButtonClearance: Dp = 96.dp,
        /** Width of a centred call-to-action relative to its container. */
        val narrowContentFraction: Float = 0.6f,
    )

    /** Metrics of the token showcase in the Graphite catalog. */
    @Immutable
    data class Catalog(
        val swatchWidth: Dp = 72.dp,
        val swatchHeight: Dp = 48.dp,
        val sampleLabelColumn: Dp = 72.dp,
    )
}
