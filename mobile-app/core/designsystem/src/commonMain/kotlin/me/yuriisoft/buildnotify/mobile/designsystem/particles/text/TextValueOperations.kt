package me.yuriisoft.buildnotify.mobile.designsystem.particles.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle

/** This text followed by [other], with nothing in between. */
operator fun TextValue.plus(other: TextValue): TextValue = TextValue.Joined(listOf(this, other))

/** This text with [style] over all of it. */
fun TextValue.styled(style: SpanStyle): TextValue = TextValue.Styled(this, style)

/** These texts with [separator] between them. */
fun List<TextValue>.joined(separator: TextValue = TextValue.Plain("")): TextValue = TextValue.Joined(this, separator)

/** A format argument resolved in composition: nested [TextValue]s become their text. */
@Composable
internal fun Any.resolved(): Any = if (this is TextValue) string() else this

/** A format argument resolved outside composition. */
internal suspend fun Any.loaded(): Any = if (this is TextValue) load() else this
