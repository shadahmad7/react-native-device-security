package com.shadahmad7.reactnativedevicesecurity

class ReactNativeDeviceSecurityException(
    val code: String,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    companion object {
        const val DETECTION_FAILED = "DEVICE_SECURITY_DETECTION_FAILED"
    }
}