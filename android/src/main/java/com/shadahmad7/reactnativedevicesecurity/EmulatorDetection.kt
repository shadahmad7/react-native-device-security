// EmulatorDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.os.Build

object EmulatorDetection {

    fun isEmulator(): Boolean {
        return try {
            val fingerprint = Build.FINGERPRINT.orEmpty()
            val model = Build.MODEL.orEmpty()
            val manufacturer = Build.MANUFACTURER.orEmpty()
            val brand = Build.BRAND.orEmpty()
            val device = Build.DEVICE.orEmpty()
            val product = Build.PRODUCT.orEmpty()
            val hardware = Build.HARDWARE.orEmpty()

            fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                fingerprint.contains("emulator", ignoreCase = true) ||
                model.contains("google_sdk", ignoreCase = true) ||
                model.contains("emulator", ignoreCase = true) ||
                model.contains("android sdk", ignoreCase = true) ||
                manufacturer.contains("Genymotion", ignoreCase = true) ||
                brand.startsWith("generic") ||
                device.startsWith("generic") ||
                product.contains("sdk", ignoreCase = true) ||
                product.contains("emulator", ignoreCase = true) ||
                hardware.contains("goldfish", ignoreCase = true) ||
                hardware.contains("ranchu", ignoreCase = true)
        } catch (e: Exception) {
            throw ReactNativeDeviceSecurityException(
                "Failed to detect emulator status.",
                e
            )
        }
    }
}