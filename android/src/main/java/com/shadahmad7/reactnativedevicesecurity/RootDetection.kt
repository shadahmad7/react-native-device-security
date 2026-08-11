// RootDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.os.Build
import java.io.File

object RootDetection {

    fun isRooted(packageManager: android.content.pm.PackageManager): Boolean {
        return try {
            checkRootManagementApps(packageManager) ||
                checkDangerousProps() ||
                checkSuBinary() ||
                checkSuCommand() ||
                checkWritableSystemDirectories()
        } catch (e: Exception) {
            throw ReactNativeDeviceSecurityException(
                "Failed to detect root status.",
                e
            )
        }
    }
    
    private fun checkRootManagementApps(
        packageManager: android.content.pm.PackageManager
    ): Boolean {
        val packages = listOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk"
        )

        return packages.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun checkDangerousProps(): Boolean {
        return try {
            val buildTags = Build.TAGS.orEmpty()

            buildTags.contains("test-keys")
        } catch (_: Exception) {
            false
        }
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/su",
            "/system/usr/we-need-root/su",
            "/system/xbin/mu",
            "/vendor/bin/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su"
        )

        return paths.any { path ->
            File(path).exists()
        }
    }

    private fun checkSuCommand(): Boolean {
        return try {
            Runtime.getRuntime()
                .exec(arrayOf("which", "su"))
                .inputStream
                .bufferedReader()
                .readLine() != null
        } catch (_: Exception) {
            false
        }
    }

    private fun checkWritableSystemDirectories(): Boolean {
        val directories = listOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin"
        )

        return directories.any { path ->
            try {
                File(path).canWrite()
            } catch (_: Exception) {
                false
            }
        }
    }
}