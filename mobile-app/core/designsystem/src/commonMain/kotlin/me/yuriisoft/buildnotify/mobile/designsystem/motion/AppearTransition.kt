package me.yuriisoft.buildnotify.mobile.designsystem.motion

/** How [Appear] brings content in and out. */
enum class AppearTransition {
    /** Fade only — scrims, labels. */
    Fade,

    /** Fade and scale from 80 % — floating buttons, badges. */
    Pop,

    /** Slide in from the bottom edge — bottom sheets. */
    Sheet,
}
