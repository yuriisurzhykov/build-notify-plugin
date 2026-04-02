package me.yuriisoft.buildnotify.mobile.feature.networkstatus.di

/**
 * Contribution interface for the Network-Status feature.
 *
 * This feature has no injectable classes — [NetworkStatusEffect] is a
 * `@Composable` function whose dependencies ([ConnectionManager],
 * [ToastHostState], [Navigator]) are passed directly at the call site.
 *
 * The interface exists as a marker so that `AppComponent` can express
 * the dependency relationship in the same way it does for every other
 * feature module, keeping the graph declaration uniform.
 */
interface NetworkStatusComponent
