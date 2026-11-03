package me.yuriisoft.buildnotify.mobile.designsystem.particles.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A piece of user-facing text, described rather than resolved: a plain string, a string
 * or plural resource with arguments, a composition of parts, or any of these with a span
 * style. Components take [TextValue] and resolve it in composition ([annotated], [string]);
 * code outside composition (view models, notifications) uses [load].
 *
 * Arguments may themselves be [TextValue]s; they are resolved before formatting.
 *
 * ```kotlin
 * TextValue.of(Res.string.build_failed) + TextValue.of(Res.string.minutes_ago, 2)
 * TextValue.of(Res.plurals.errors, count, count).styled(SpanStyle(fontWeight = Bold))
 * ```
 */
@Immutable
sealed interface TextValue {

    /** The text with its spans, for display. */
    @Composable
    fun annotated(): AnnotatedString

    /** The plain text, for semantics, clipboard and string-only APIs. */
    @Composable
    fun string(): String = annotated().text

    /** The plain text outside composition. */
    suspend fun load(): String

    /** Text that needs no resolution, e.g. a project name or a task path. */
    @Immutable
    data class Plain(val value: String) : TextValue {
        @Composable
        override fun annotated(): AnnotatedString = AnnotatedString(value)

        override suspend fun load(): String = value
    }

    /** A string resource, formatted with [args]. */
    @Immutable
    data class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : TextValue {
        @Composable
        override fun annotated(): AnnotatedString = AnnotatedString(string())

        @Composable
        override fun string(): String = stringResource(resource, *args.map { it.resolved() }.toTypedArray())

        override suspend fun load(): String = getString(resource, *args.map { it.loaded() }.toTypedArray())
    }

    /** A plural resource for [quantity], formatted with [args]. */
    @Immutable
    data class Plural(
        val resource: PluralStringResource,
        val quantity: Int,
        val args: List<Any> = emptyList(),
    ) : TextValue {
        @Composable
        override fun annotated(): AnnotatedString = AnnotatedString(string())

        @Composable
        override fun string(): String =
            pluralStringResource(resource, quantity, *args.map { it.resolved() }.toTypedArray())

        override suspend fun load(): String =
            getPluralString(resource, quantity, *args.map { it.loaded() }.toTypedArray())
    }

    /** [parts] one after another, with [separator] between them; spans of each part are kept. */
    @Immutable
    data class Joined(val parts: List<TextValue>, val separator: TextValue = Plain("")) : TextValue {
        @Composable
        override fun annotated(): AnnotatedString {
            val resolved = parts.map { it.annotated() }
            val glue = separator.annotated()
            return buildAnnotatedString {
                resolved.forEachIndexed { index, part ->
                    if (index > 0) append(glue)
                    append(part)
                }
            }
        }

        override suspend fun load(): String {
            val glue = separator.load()
            return parts.map { it.load() }.joinToString(glue)
        }
    }

    /** [text] with [style] applied over all of it, on top of its own spans. */
    @Immutable
    data class Styled(val text: TextValue, val style: SpanStyle) : TextValue {
        @Composable
        override fun annotated(): AnnotatedString {
            val inner = text.annotated()
            return buildAnnotatedString { withStyle(style) { append(inner) } }
        }

        override suspend fun load(): String = text.load()
    }

    companion object {
        /** The separator of inline metadata: "shop-android · 1m 45s · 2 min ago". */
        val DotSeparator: TextValue = Plain(" · ")

        fun of(value: String): TextValue = Plain(value)

        fun of(resource: StringResource, vararg args: Any): TextValue = Resource(resource, args.toList())

        fun of(resource: PluralStringResource, quantity: Int, vararg args: Any): TextValue =
            Plural(resource, quantity, args.toList())

        /** [parts] joined by [separator]. */
        fun join(parts: List<TextValue>, separator: TextValue = Plain("")): TextValue = Joined(parts, separator)
    }
}
