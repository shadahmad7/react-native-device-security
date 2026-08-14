// RootDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object RootDetection {

    data class Checks(
        val rootManagementApp: Boolean,
        val dangerousBuildTags: Boolean,
        val suBinary: Boolean,
        val suCommand: Boolean,
        val writableSystemDirectories: Boolean,
        val dangerousProperties: Boolean,
        val rootFiles: Boolean,
        val rwSystemMounts: Boolean,
    )

    data class Result(
        val isRooted: Boolean,
        val checks: Checks,
    )

    fun getResult(
        packageManager: PackageManager,
    ): Result {
        return try {
            val checks = Checks(
                rootManagementApp =
                    checkRootManagementApps(packageManager),

                dangerousBuildTags =
                    checkDangerousBuildTags(),

                suBinary =
                    checkSuBinary(),

                suCommand =
                    checkSuCommand(),

                writableSystemDirectories =
                    checkWritableSystemDirectories(),

                dangerousProperties =
                    checkDangerousProperties(),

                rootFiles =
                    checkRootFiles(),

                rwSystemMounts =
                    checkRwSystemMounts(),
            )

            Result(
                isRooted = checks.isRooted(),
                checks = checks,
            )
        } catch (e: Exception) {
            throw ReactNativeDeviceSecurityException(
                "Failed to detect root status.",
                e
            )
        }
    }

    fun isRooted(
        packageManager: PackageManager,
    ): Boolean {
        return getResult(packageManager).isRooted
    }

    private fun Checks.isRooted(): Boolean {
        return rootManagementApp ||
            suBinary ||
            suCommand ||
            writableSystemDirectories ||
            dangerousProperties ||
            rootFiles ||
            rwSystemMounts ||
            dangerousBuildTags
    }

    /**
     * Detects commonly known root-management applications.
     */
    private fun checkRootManagementApps(
        packageManager: PackageManager,
    ): Boolean {
        val packages = listOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.topjohnwu.magisk.debug",
            "io.github.vvb2060.magisk",
            "me.weishu.kernelsu",
            "com.rifsxd.ksunext",
        )

        return packages.any { packageName ->
            try {
                packageManager.getPackageInfo(
                    packageName,
                    0
                )

                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Detects non-production Android build keys.
     */
    private fun checkDangerousBuildTags(): Boolean {
        return Build.TAGS
            ?.contains(
                "test-keys",
                ignoreCase = true
            )
            ?: false
    }

    /**
     * Detects known su binary locations.
     */
    private fun checkSuBinary(): Boolean {
        val paths = listOf(
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
            "/data/local/xbin/su",
            "/product/bin/su",
            "/odm/bin/su",
            "/su/bin/su",
        )

        return paths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Attempts to locate the su executable.
     */
    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf(
                    "sh",
                    "-c",
                    "command -v su"
                )
            )

            val output =
                process.inputStream
                    .bufferedReader()
                    .use { it.readText() }

            process.waitFor()

            process.exitValue() == 0 &&
                output.isNotBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Detects system directories that should normally be read-only.
     */
    private fun checkWritableSystemDirectories(): Boolean {
        val directories = listOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor",
            "/vendor/bin",
            "/sbin",
            "/product",
            "/odm",
        )

        return directories.any { path ->
            try {
                val file = File(path)

                file.exists() && file.canWrite()
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Detects suspicious Android system properties.
     */
    private fun checkDangerousProperties(): Boolean {
        return try {
            val properties = listOf(
                "ro.debuggable",
                "ro.secure",
                "ro.build.type",
            )

            properties.any { property ->
                val value = getSystemProperty(property)

                when (property) {
                    "ro.debuggable" ->
                        value == "1"

                    "ro.secure" ->
                        value == "0"

                    "ro.build.type" ->
                        value.equals("eng", ignoreCase = true) ||
                            value.equals(
                                "userdebug",
                                ignoreCase = true
                            )

                    else ->
                        false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Detects commonly associated root files/directories.
     */
    private fun checkRootFiles(): Boolean {
        val paths = listOf(
            "/data/adb",
            "/data/adb/magisk",
            "/data/adb/ksu",
            "/data/adb/ksud",
            "/cache/su",
            "/dev/com.koushikdutta.superuser.daemon/",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/etc/init.d",
        )

        return paths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Detects read-write system mounts.
     */
    private fun checkRwSystemMounts(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("mount")
            )

            process.inputStream
                .bufferedReader()
                .useLines { lines ->
                    lines.any { line ->
                        val normalized =
                            line.lowercase()

                        val isSystemMount =
                            normalized.contains("/system") ||
                                normalized.contains("/vendor") ||
                                normalized.contains("/product")

                        val isReadWrite =
                            normalized.contains(" rw,") ||
                                normalized.contains("(rw,") ||
                                normalized.contains(",rw ") ||
                                normalized.contains(",rw)")

                        isSystemMount && isReadWrite
                    }
                }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reads an Android system property.
     */
    private fun getSystemProperty(
        property: String,
    ): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("getprop", property)
            )

            process.inputStream
                .bufferedReader()
                .use {
                    it.readLine()
                        ?.trim()
                        .orEmpty()
                }
        } catch (_: Exception) {
            ""
        }
    }
}