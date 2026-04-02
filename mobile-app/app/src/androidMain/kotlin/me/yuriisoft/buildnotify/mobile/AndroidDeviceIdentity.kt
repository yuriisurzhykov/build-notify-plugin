package me.yuriisoft.buildnotify.mobile

import android.os.Build
import me.yuriisoft.buildnotify.mobile.core.platform.DeviceIdentity

/**
 * Reads the device model from [Build.MANUFACTURER] and [Build.MODEL].
 *
 * [Build.MODEL] often already contains the marketing name (e.g. "Pixel 9 Pro"),
 * but prepending the capitalised [Build.MANUFACTURER] makes the string
 * unambiguous for devices where the model number is opaque (e.g. Samsung's
 * `SM-S928B`).
 */
class AndroidDeviceIdentity : DeviceIdentity {

    override val deviceName: String =
        "${Build.MANUFACTURER.replaceFirstChar { it.titlecase() }} ${Build.MODEL}"

    override val platform: String = "android"
}
