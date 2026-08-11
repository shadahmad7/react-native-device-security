// ReactNativeDeviceSecuritModule.kt

package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.bridge.ReactApplicationContext

class ReactNativeDeviceSecurityModule(
  reactContext: ReactApplicationContext
) : NativeReactNativeDeviceSecuritySpec(reactContext) {

  override fun getSecurityStatus(promise: Promise) {
    try {
      val isRooted = RootDetection.isRooted()
      val isEmulator = EmulatorDetection.isEmulator()

      val status = Arguments.createMap().apply {
        putBoolean("isCompromised", isRooted)
        putBoolean("isRooted", isRooted)
        putBoolean("isJailbroken", false)
        putBoolean("isEmulator", isEmulator)
      }

      promise.resolve(status)
    } catch (e: Exception) {
      promise.reject(
        "DEVICE_SECURITY_DETECTION_FAILED",
        ReactNativeDeviceSecurityException.detectionFailed,
        e
      )
    }
  }

  companion object {
    const val NAME = "ReactNativeDeviceSecurity"
  }

  override fun getName(): String = NAME
}