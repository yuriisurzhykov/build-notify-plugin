package me.yuriisoft.buildnotify.mobile

import me.yuriisoft.buildnotify.mobile.core.platform.DeviceIdentity
import platform.UIKit.UIDevice

/**
 * Reads the device name from [UIDevice.currentDevice].
 *
 * [UIDevice.name] returns the user-assigned device name (e.g. "John's iPhone").
 * This is more recognisable to the user than the generic [UIDevice.model]
 * ("iPhone") and matches what appears in iOS Settings.
 */
class IosDeviceIdentity : DeviceIdentity {

    override val deviceName: String = UIDevice.currentDevice.name

    override val platform: String = "ios"
}
