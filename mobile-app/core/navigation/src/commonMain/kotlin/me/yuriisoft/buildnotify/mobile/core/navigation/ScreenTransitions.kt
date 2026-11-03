package me.yuriisoft.buildnotify.mobile.core.navigation

/**
 * How a screen enters and leaves. Only the kind is declared here; the animation is built
 * with the theme's motion tokens where the navigation host is composed
 * (see [enterSpec] and friends).
 */
sealed interface ScreenTransitions {
    /** The navigation library's default. */
    data object None : ScreenTransitions

    /** Pushed screens slide in from the right and back out to the right. */
    data object SlideHorizontal : ScreenTransitions

    data object Fade : ScreenTransitions
}
