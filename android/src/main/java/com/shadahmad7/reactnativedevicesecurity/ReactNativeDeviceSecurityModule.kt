// ReactNativeDeviceSecuritModule.kt

package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap

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

            val isEmulator =
                EmulatorDetection.isEmulator()

            val rootReasons =
                rootDetectionResult.reasons

            val emulatorReasons =
                if (isEmulator) {
                    EmulatorDetection.getReasons()
                } else {
                    emptyList()
                }

            val jailbreakReasons =
                emptyList<String>()

            val compromiseReasons =
                buildList {
                    if (isRooted) {
                        addAll(rootReasons)
                    }

                    if (isEmulator) {
                        addAll(emulatorReasons)
                    }

                    if (jailbreakReasons.isNotEmpty()) {
                        addAll(jailbreakReasons)
                    }
                }.distinct()

            val isCompromised =
                isRooted ||
                    isEmulator ||
                    jailbreakReasons.isNotEmpty()

            val status = Arguments.createMap().apply {
                putBoolean(
                    "isCompromised",
                    isCompromised
                )

                putBoolean(
                    "isRooted",
                    isRooted
                )

                putBoolean(
                    "isJailbroken",
                    false
                )

                putBoolean(
                    "isEmulator",
                    isEmulator
                )

                putStringArray(
                    "rootReasons",
                    rootReasons
                )

                putStringArray(
                    "jailbreakReasons",
                    jailbreakReasons
                )

                putStringArray(
                    "emulatorReasons",
                    emulatorReasons
                )

                putStringArray(
                    "compromiseReasons",
                    compromiseReasons
                )
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
            val result =
                RootDetection.getResult(
                    reactApplicationContext.packageManager
                )

            promise.resolve(
                createSecurityCheckResult(
                    detected = result.isRooted,
                    reasons = result.reasons
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
        // Android does not perform jailbreak detection.
        promise.resolve(
            createSecurityCheckResult(
                detected = false,
                reasons = emptyList()
            )
        )
    }

    override fun isEmulator(promise: Promise) {
        try {
            val isEmulator =
                EmulatorDetection.isEmulator()

            val reasons =
                if (isEmulator) {
                    EmulatorDetection.getReasons()
                } else {
                    emptyList()
                }

            promise.resolve(
                createSecurityCheckResult(
                    detected = isEmulator,
                    reasons = reasons
                )
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
            val rootDetectionResult =
                RootDetection.getResult(
                    reactApplicationContext.packageManager
                )

            val isEmulator =
                EmulatorDetection.isEmulator()

            val rootReasons =
                rootDetectionResult.reasons

            val emulatorReasons =
                if (isEmulator) {
                    EmulatorDetection.getReasons()
                } else {
                    emptyList()
                }

            val compromiseReasons =
                buildList {
                    if (rootDetectionResult.isRooted) {
                        addAll(rootReasons)
                    }

                    if (isEmulator) {
                        addAll(emulatorReasons)
                    }
                }.distinct()

            val isCompromised =
                rootDetectionResult.isRooted ||
                    isEmulator

            promise.resolve(
                createSecurityCheckResult(
                    detected = isCompromised,
                    reasons = compromiseReasons
                )
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
                putBoolean(
                    "isRooted",
                    result.isRooted
                )

                putStringArray(
                    "reasons",
                    result.reasons
                )

                putMap(
                    "checks",
                    checks
                )
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

    private fun createSecurityCheckResult(
        detected: Boolean,
        reasons: List<String>
    ): WritableMap {
        return Arguments.createMap().apply {
            putBoolean(
                "detected",
                detected
            )

            putStringArray(
                "reasons",
                reasons
            )
        }
    }

    private fun WritableMap.putStringArray(
        key: String,
        values: List<String>
    ) {
        putArray(
            key,
            Arguments.createArray().apply {
                values.forEach { value ->
                    pushString(value)
                }
            }
        )
    }

    companion object {
        const val NAME = "ReactNativeDeviceSecurity"
    }
}