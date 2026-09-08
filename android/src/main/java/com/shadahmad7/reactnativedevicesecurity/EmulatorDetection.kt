// EmulatorDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.os.Build

object EmulatorDetection {

    data class Result(
        val isEmulator: Boolean,
        val reasons: List<String>
    )

    fun getResult(): Result {
        return try {
            val fingerprint = Build.FINGERPRINT.orEmpty()
            val model = Build.MODEL.orEmpty()
            val manufacturer = Build.MANUFACTURER.orEmpty()
            val brand = Build.BRAND.orEmpty()
            val device = Build.DEVICE.orEmpty()
            val product = Build.PRODUCT.orEmpty()
            val hardware = Build.HARDWARE.orEmpty()

            val reasons = mutableListOf<String>()

            if (fingerprint.startsWith("generic")) {
                reasons.add("EMULATOR_FINGERPRINT_GENERIC")
            }

            if (fingerprint.startsWith("unknown")) {
                reasons.add("EMULATOR_FINGERPRINT_UNKNOWN")
            }

            if (fingerprint.contains("emulator", ignoreCase = true)) {
                reasons.add("EMULATOR_FINGERPRINT_EMULATOR")
            }

            if (model.contains("google_sdk", ignoreCase = true)) {
                reasons.add("EMULATOR_MODEL_GOOGLE_SDK")
            }

            if (model.contains("emulator", ignoreCase = true)) {
                reasons.add("EMULATOR_MODEL")
            }

            if (model.contains("android sdk", ignoreCase = true)) {
                reasons.add("EMULATOR_MODEL_ANDROID_SDK")
            }

            if (manufacturer.contains("Genymotion", ignoreCase = true)) {
                reasons.add("EMULATOR_MANUFACTURER_GENYMOTION")
            }

            if (brand.startsWith("generic")) {
                reasons.add("EMULATOR_BRAND_GENERIC")
            }

            if (device.startsWith("generic")) {
                reasons.add("EMULATOR_DEVICE_GENERIC")
            }

            if (product.contains("sdk", ignoreCase = true)) {
                reasons.add("EMULATOR_PRODUCT_SDK")
            }

            if (product.contains("emulator", ignoreCase = true)) {
                reasons.add("EMULATOR_PRODUCT")
            }

            if (hardware.contains("goldfish", ignoreCase = true)) {
                reasons.add("EMULATOR_HARDWARE_GOLDFISH")
            }

            if (hardware.contains("ranchu", ignoreCase = true)) {
                reasons.add("EMULATOR_HARDWARE_RANCHU")
            }

            Result(
                isEmulator = reasons.isNotEmpty(),
                reasons = reasons.distinct()
            )

        } catch (e: Exception) {
            throw ReactNativeDeviceSecurityException(
                "Failed to detect emulator status.",
                e
            )
        }
    }

    fun isEmulator(): Boolean {
        return getResult().isEmulator
    }

    fun getReasons(): List<String> {
        return getResult().reasons
    }
}