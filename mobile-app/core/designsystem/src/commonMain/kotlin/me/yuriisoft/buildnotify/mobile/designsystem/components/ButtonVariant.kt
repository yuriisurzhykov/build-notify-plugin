package me.yuriisoft.buildnotify.mobile.designsystem.components

/**
 * - [Primary]   — the one action the screen exists for ("Передать агенту"). Max one per screen.
 * - [Secondary] — neutral filled action ("Лог").
 * - [Outline]   — neutral action on a tinted hero ("Перезапустить").
 * - [Danger]    — destructive, never filled ("Остановить").
 */
enum class ButtonVariant {
    Primary,
    Secondary,
    Outline,
    Danger,
}
