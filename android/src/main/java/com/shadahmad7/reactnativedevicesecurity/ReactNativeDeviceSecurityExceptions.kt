// ReactNativeDeviceSecurityExceptions.kt

package com.shadahmad7.reactnativedevicesecurity

class ReactNativeDeviceSecurityException(
  message: String
) : Exception(message) {

  companion object {
    const val detectionFailed =
      "Unable to determine device security status."
  }
}