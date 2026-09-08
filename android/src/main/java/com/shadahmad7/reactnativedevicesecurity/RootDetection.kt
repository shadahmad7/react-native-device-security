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
        val reasons: List<String>,
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

            val reasons =
                checks.getReasons()

            /*
             * Only strong indicators should determine whether
             * the device is actually considered rooted.
             *
             * The following checks are intentionally diagnostic:
             *
             * - dangerousBuildTags
             * - dangerousProperties
             * - rootFiles
             * - rwSystemMounts
             * - writableSystemDirectories
             *
             * Modern Android devices can legitimately expose
             * filesystem/mount/property characteristics that
             * would otherwise create false positives.
             */
            val isRooted =
                checks.rootManagementApp ||
                checks.suBinary ||
                checks.suCommand

            Result(
                isRooted = isRooted,
                reasons = reasons,
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

    // -------------------------------------------------------------------------
    // Reasons
    // -------------------------------------------------------------------------

    private fun Checks.getReasons(): List<String> {
        return buildList {

            if (rootManagementApp) {
                add("ROOT_MANAGEMENT_APP_DETECTED")
            }

            if (dangerousBuildTags) {
                add("DANGEROUS_BUILD_TAGS_DETECTED")
            }

            if (suBinary) {
                add("SU_BINARY_DETECTED")
            }

            if (suCommand) {
                add("SU_COMMAND_DETECTED")
            }

            /*
             * Diagnostic only.
             *
             * Do not use this as a standalone root signal.
             */
            if (writableSystemDirectories) {
                add("WRITABLE_SYSTEM_DIRECTORY_DETECTED")
            }

            /*
             * Diagnostic only.
             *
             * Modern Android devices can expose properties such as
             * ro.debuggable without necessarily being user-rooted.
             */
            if (dangerousProperties) {
                add("DANGEROUS_SYSTEM_PROPERTY_DETECTED")
            }

            /*
             * Diagnostic only.
             *
             * Files under /data/adb or similar locations can sometimes
             * be visible depending on Android implementation/mount
             * namespace and should not alone determine root status.
             */
            if (rootFiles) {
                add("ROOT_FILE_DETECTED")
            }

            /*
             * Diagnostic only.
             *
             * Modern Android uses dynamic partitions and complex mount
             * namespaces. An rw mount does not necessarily mean the
             * application has root privileges.
             */
            if (rwSystemMounts) {
                add("RW_SYSTEM_MOUNT_DETECTED")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Root management apps
    // -------------------------------------------------------------------------

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
                    0,
                )

                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            } catch (_: Exception) {
                false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Build tags
    // -------------------------------------------------------------------------

    private fun checkDangerousBuildTags(): Boolean {
        return Build.TAGS
            ?.split(",")
            ?.any {
                it.trim().equals(
                    "test-keys",
                    ignoreCase = true,
                )
            }
            ?: false
    }

    // -------------------------------------------------------------------------
    // su binary
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // su command
    // -------------------------------------------------------------------------

    private fun checkSuCommand(): Boolean {
        return try {
            val process =
                Runtime.getRuntime().exec(
                    arrayOf(
                        "sh",
                        "-c",
                        "command -v su",
                    ),
                )

            val output =
                process.inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

            process.waitFor()

            process.exitValue() == 0 &&
                output.isNotBlank()

        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------------------
    // Writable system directories
    // -------------------------------------------------------------------------

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

                file.exists() &&
                    file.canWrite()

            } catch (_: Exception) {
                false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Dangerous properties
    // -------------------------------------------------------------------------

    private fun checkDangerousProperties(): Boolean {
        return try {
            val debuggable =
                getSystemProperty("ro.debuggable")

            val secure =
                getSystemProperty("ro.secure")

            val buildType =
                getSystemProperty("ro.build.type")

            /*
             * These are supporting indicators only.
             *
             * They are NOT sufficient to classify the device as rooted.
             */
            debuggable == "1" ||
                secure == "0" ||
                buildType.equals(
                    "eng",
                    ignoreCase = true,
                ) ||
                buildType.equals(
                    "userdebug",
                    ignoreCase = true,
                )

        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------------------
    // Root files
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // RW system mounts
    // -------------------------------------------------------------------------

    private fun checkRwSystemMounts(): Boolean {
        return try {
            val process =
                Runtime.getRuntime().exec(
                    arrayOf("mount"),
                )

            process.inputStream
                .bufferedReader()
                .useLines { lines ->

                    lines.any { line ->
                        isSuspiciousRwSystemMount(line)
                    }
                }

        } catch (_: Exception) {
            false
        }
    }

    private fun isSuspiciousRwSystemMount(
        line: String,
    ): Boolean {
        val normalized =
            line.lowercase()

        val isSystemMount =
            normalized.contains(" /system ") ||
                normalized.contains(" /vendor ") ||
                normalized.contains(" /product ") ||
                normalized.contains(" /odm ")

        val isReadWrite =
            normalized.contains(" rw,") ||
                normalized.contains(",rw ") ||
                normalized.contains("(rw,") ||
                normalized.contains(",rw)")

        return isSystemMount &&
            isReadWrite
    }

    // -------------------------------------------------------------------------
    // System property
    // -------------------------------------------------------------------------

    private fun getSystemProperty(
        property: String,
    ): String {
        return try {
            val process =
                Runtime.getRuntime().exec(
                    arrayOf(
                        "getprop",
                        property,
                    ),
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