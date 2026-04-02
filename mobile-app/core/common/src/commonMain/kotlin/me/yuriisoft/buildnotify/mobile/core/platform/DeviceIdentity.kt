package me.yuriisoft.buildnotify.mobile.core.platform

/**
 * Platform-provided device metadata used to identify this client to the
 * IDE plugin during the `sys.hello` exchange.
 *
 * Implementations live in platform source sets (androidMain / iosMain)
 * and are passed into the DI graph via the [AppComponent] constructor,
 * following the same pattern as [AppVersionProvider].
 */
interface DeviceIdentity {
    /** Human-readable device model, e.g. "Google Pixel 9 Pro" or "iPhone 16 Pro". */
    val deviceName: String
    /** Platform identifier: `"android"` or `"ios"`. */
    val platform: String
}
