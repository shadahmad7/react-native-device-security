package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.ReactContextBaseJavaModule

class ReactNativeDeviceSecurityModule(
    private val reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "ReactNativeDeviceSecurity"
    }

    fun getSecurityStatus(promise: Promise) {
        try {
            val isRooted = RootDetection.isRooted(reactContext)
            val isEmulator = EmulatorDetection.isEmulator()

            val result = WritableNativeMap().apply {
                putBoolean("isCompromised", isRooted || isEmulator)
                putBoolean("isRooted", isRooted)
                putBoolean("isJailbroken", false)
                putBoolean("isEmulator", isEmulator)
            }

            promise.resolve(result)
        } catch (error: ReactNativeDeviceSecurityException) {
            promise.reject(
                error.code,
                error.message,
                error,
            )
        } catch (error: Throwable) {
            promise.reject(
                ReactNativeDeviceSecurityException.DETECTION_FAILED,
                "Failed to determine device security status.",
                error,
            )
        }
    }
}