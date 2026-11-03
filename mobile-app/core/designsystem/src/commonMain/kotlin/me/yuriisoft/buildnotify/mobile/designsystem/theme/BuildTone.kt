package me.yuriisoft.buildnotify.mobile.designsystem.theme

/**
 * Semantic state of a build — the only reason colour appears in Graphite.
 * Every status-coloured component takes a [BuildTone] instead of raw colours.
 */
enum class BuildTone {
    Running,
    Success,
    Failure,
    Warning,
    Neutral,
}
