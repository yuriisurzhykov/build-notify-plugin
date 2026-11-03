package me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons

/**
 * - [Primary]   — the one action the screen exists for ("Hand off to an agent"). Max one per screen.
 * - [Secondary] — neutral filled action ("Log").
 * - [Outline]   — neutral action on a tinted hero ("Rerun").
 * - [Danger]    — destructive, never filled ("Stop").
 */
enum class ButtonVariant {
    Primary,
    Secondary,
    Outline,
    Danger,
}
