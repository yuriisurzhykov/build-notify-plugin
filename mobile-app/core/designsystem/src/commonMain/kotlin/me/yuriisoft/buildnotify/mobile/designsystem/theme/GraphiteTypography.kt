package me.yuriisoft.buildnotify.mobile.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.inter_bold
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.inter_medium
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.inter_regular
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.inter_semibold
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

/**
 * Graphite type scale.
 *
 * Inter carries the interface; JetBrains Mono carries everything that came from
 * Gradle — task paths, durations, timers and logs ([code], [log], [timer], [mono]).
 */
@Immutable
data class GraphiteTypography(
    /** 30/36 bold — the hero statement ("Всё собрано"). */
    val display: TextStyle,
    /** 22/28 semibold — screen and sheet titles. */
    val headline: TextStyle,
    /** 17/22 semibold — project names in lists and cards. */
    val title: TextStyle,
    /** 15/21 — default body. */
    val body: TextStyle,
    /** 15/21 medium — list row titles. */
    val bodyStrong: TextStyle,
    /** 14/20 — error messages, secondary paragraphs. */
    val bodySmall: TextStyle,
    /** 13/18 — metadata ("Успешно · 1m 45s"). */
    val meta: TextStyle,
    /** 13/18 semibold — section headers, status labels. */
    val label: TextStyle,
    /** 12/16 — hints under buttons. */
    val caption: TextStyle,
    /** 12/16 semibold — status pills and tags. */
    val tag: TextStyle,
    /** 14/20 semibold — chips and choice cards. */
    val chip: TextStyle,
    /** 15/20 semibold — regular buttons. */
    val button: TextStyle,
    /** 16/20 semibold — large primary buttons. */
    val buttonLarge: TextStyle,
    /** 64/70 mono — the running-build timer. */
    val timer: TextStyle,
    /** 13/18 mono — task paths, durations, file:line. */
    val mono: TextStyle,
    /** 14/20 mono — tappable task names. */
    val code: TextStyle,
    /** 12/17 mono — log lines. */
    val log: TextStyle,
    /** 26/32 mono — one digit of a pairing PIN. */
    val pin: TextStyle,
)

@Composable
internal fun rememberGraphiteTypography(): GraphiteTypography {
    val inter = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold),
    )
    val mono = FontFamily(Font(Res.font.jetbrains_mono_regular, FontWeight.Normal))
    return graphiteTypography(inter, mono)
}

/** Builds the scale from explicit families; also used by previews and tests. */
fun graphiteTypography(sans: FontFamily, mono: FontFamily): GraphiteTypography = GraphiteTypography(
    display = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.02).em),
    headline = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em),
    title = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    meta = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    label = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    chip = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    tag = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    button = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    buttonLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
    timer = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 64.sp, lineHeight = 70.sp, letterSpacing = (-0.03).em),
    mono = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    code = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    log = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    pin = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 32.sp),
)
