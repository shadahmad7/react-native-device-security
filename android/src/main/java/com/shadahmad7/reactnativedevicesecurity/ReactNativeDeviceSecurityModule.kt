// ReactNativeDeviceSecuritModule.kt

package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext

class ReactNativeDeviceSecurityModule(
    reactContext: ReactApplicationContext
) : NativeReactNativeDeviceSecuritySpec(reactContext) {

    override fun getName(): String {
        return NAME
    }

    override fun getSecurityStatus(promise: Promise) {
        try {
            val rootDetectionResult =
                RootDetection.getResult(
                    reactApplicationContext.packageManager
                )

            val isRooted = rootDetectionResult.isRooted
            val isEmulator = EmulatorDetection.isEmulator()

            val status = Arguments.createMap().apply {
                putBoolean(
                    "isCompromised",
                    isRooted
                )
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

    override fun isRooted(promise: Promise) {
        try {
            promise.resolve(
                RootDetection.isRooted(
                    reactApplicationContext.packageManager
                )
            )
        } catch (e: ReactNativeDeviceSecurityException) {
            promise.reject(
                "ROOT_DETECTION_FAILED",
                e.message,
                e
            )
        } catch (e: Exception) {
            promise.reject(
                "ROOT_DETECTION_FAILED",
                "Unable to determine root status.",
                e
            )
        }
    }

    override fun isJailbroken(promise: Promise) {
        // Jailbreak detection is iOS-specific.
        // Android always returns false.
        promise.resolve(false)
    }

    override fun isEmulator(promise: Promise) {
        try {
            promise.resolve(
                EmulatorDetection.isEmulator()
            )
        } catch (e: ReactNativeDeviceSecurityException) {
            promise.reject(
                "EMULATOR_DETECTION_FAILED",
                e.message,
                e
            )
        } catch (e: Exception) {
            promise.reject(
                "EMULATOR_DETECTION_FAILED",
                "Unable to determine emulator status.",
                e
            )
        }
    }

    override fun isSecurityCompromised(promise: Promise) {
        try {
            val isRooted =
                RootDetection.isRooted(
                    reactApplicationContext.packageManager
                )

            val isEmulator =
                EmulatorDetection.isEmulator()

            promise.resolve(
                isRooted || isEmulator
            )
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

    override fun getRootDetectionResult(promise: Promise) {
        try {
            val result =
                RootDetection.getResult(
                    reactApplicationContext.packageManager
                )

            val checks = Arguments.createMap().apply {
                putBoolean(
                    "rootManagementApp",
                    result.checks.rootManagementApp
                )
                putBoolean(
                    "dangerousBuildTags",
                    result.checks.dangerousBuildTags
                )
                putBoolean(
                    "suBinary",
                    result.checks.suBinary
                )
                putBoolean(
                    "suCommand",
                    result.checks.suCommand
                )
                putBoolean(
                    "writableSystemDirectories",
                    result.checks.writableSystemDirectories
                )
                putBoolean(
                    "dangerousProperties",
                    result.checks.dangerousProperties
                )
                putBoolean(
                    "rootFiles",
                    result.checks.rootFiles
                )
                putBoolean(
                    "rwSystemMounts",
                    result.checks.rwSystemMounts
                )
            }

            val response = Arguments.createMap().apply {
                putBoolean("isRooted", result.isRooted)
                putMap("checks", checks)
            }

            promise.resolve(response)
        } catch (e: ReactNativeDeviceSecurityException) {
            promise.reject(
                "ROOT_DETECTION_FAILED",
                e.message,
                e
            )
        } catch (e: Exception) {
            promise.reject(
                "ROOT_DETECTION_FAILED",
                "Unable to determine root detection result.",
                e
            )
        }
    }

    companion object {
        const val NAME = "ReactNativeDeviceSecurity"
    }
}