// EmulatorDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.os.Build

object EmulatorDetection {

    fun isEmulator(): Boolean {
        return try {
            checkEmulator()
        } catch (error: Throwable) {
            throw ReactNativeDeviceSecurityException(
                code = ReactNativeDeviceSecurityException.DETECTION_FAILED,
                message = "Failed to determine emulator status.",
                cause = error,
            )
        }
    }

    private fun checkEmulator(): Boolean {
        return (
            Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (
                    Build.BRAND.startsWith("generic") &&
                        Build.DEVICE.startsWith("generic")
                ) ||
                Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("emulator")
        )
    }
}