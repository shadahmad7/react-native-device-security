// ReactNativeDeviceSecuritModule.kt

package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext

class ReactNativeDeviceSecurityModule(
    reactContext: ReactApplicationContext
) : NativeReactNativeDeviceSecuritySpec(reactContext) {

    override fun getSecurityStatus(promise: Promise) {
        try {
            val isRooted = RootDetection.isRooted(
                reactApplicationContext.packageManager
            )

            val isEmulator = EmulatorDetection.isEmulator()

            val status = com.facebook.react.bridge.Arguments.createMap().apply {
                putBoolean("isCompromised", isRooted)
                putBoolean("isRooted", isRooted)
                putBoolean("isJailbroken", false)
                putBoolean("isEmulator", isEmulator)
            }

            promise.resolve(status)
        } catch (e: ReactNativeDeviceSecurityException) {
            promise.reject(
                "DEVICE_SECURITY_DETECTION_FAILED",
                e.message,
                e
            )
        } catch (e: Exception) {
            promise.reject(
                "DEVICE_SECURITY_DETECTION_FAILED",
                "Unable to determine device security status.",
                e
            )
        }
    }

    override fun getName(): String {
        return NAME
    }

    companion object {
        const val NAME = "ReactNativeDeviceSecurity"
    }
}