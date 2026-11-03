package me.yuriisoft.buildnotify.mobile.designsystem.particles.motion

/**
 * The Graphite catalogue of content transitions for [Swap]. Features pick one of these
 * instead of composing enter/exit specs; [Swap] turns it into an animation with the
 * theme's motion tokens, so every screen moves alike.
 */
sealed interface GraphiteTransition<in S> {
    /** Plain cross-fade between two contents. */
    data object Crossfade : GraphiteTransition<Any?>

    /** The new content grows slightly into place while the size follows — hero states. */
    data object Morph : GraphiteTransition<Any?>

    /** Split-flap roll: the new value drops in from above, the old one falls out below. */
    data object Roll : GraphiteTransition<Any?>

    /** The new value rises from below — a line of text that is replaced (current task). */
    data object Rise : GraphiteTransition<Any?>

    /**
     * Step navigation inside one screen: a state with a higher [order] slides in from the
     * right, a lower one from the left; the same step cross-fades. A negative order marks
     * an out-of-band state (such as "no network"): it cross-fades in, and a lower target
     * still slides backwards.
     */
    class Directional<S>(val order: (S) -> Int) : GraphiteTransition<S>
}
