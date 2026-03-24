package me.yuriisoft.buildnotify.mobile.ui.component.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DiffLine {
    val text: String

    data class Added(override val text: String) : DiffLine
    data class Removed(override val text: String) : DiffLine
    data class Context(override val text: String) : DiffLine
}
