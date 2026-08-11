// RootDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.content.Context
import java.io.File

object RootDetection {

    fun isRooted(context: Context): Boolean {
        return try {
            checkDangerousPaths() ||
                checkSuBinary() ||
                checkTestKeys() ||
                checkRootManagementApps(context)
        } catch (error: Throwable) {
            throw ReactNativeDeviceSecurityException(
                code = ReactNativeDeviceSecurityException.DETECTION_FAILED,
                message = "Failed to determine root status.",
                cause = error,
            )
        }
    }

    private fun checkDangerousPaths(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
        )

        return paths.any { File(it).exists() }
    }

    private fun checkSuBinary(): Boolean {
        return try {
            Runtime.getRuntime()
                .exec(arrayOf("which", "su"))
                .inputStream
                .bufferedReader()
                .readLine() != null
        } catch (_: Throwable) {
            false
        }
    }
    
    private fun checkTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun checkRootManagementApps(context: Context): Boolean {
        val suspiciousPackages = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
        )

        return suspiciousPackages.any { packageName ->
            try {
                context.packageManager.getPackageInfo(
                    packageName,
                    0,
                )
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}