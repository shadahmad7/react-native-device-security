// RootDetection.kt

package com.shadahmad7.reactnativedevicesecurity

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Android root / compromise detection.
 *
 * IMPORTANT:
 * - Strong checks are used to determine isRooted.
 * - Weak/supporting checks are returned as diagnosticReasons.
 * - A diagnostic signal must NOT automatically mean the device is rooted.
 *
 * Root detection is defense-in-depth and cannot guarantee detection
 * against a sophisticated attacker who hides root from the application.
 */
object RootDetection {

    private const val TAG = "RootDetection"
    private const val COMMAND_TIMEOUT_MS = 1500L

    /**
     * Common locations used by different root implementations.
     */
    private val suPaths = listOf(
        "/system/xbin/su",
        "/system/bin/su",
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

    /**
     * Strong / high-confidence checks.
     */
    data class Checks(
        val rootManagementApp: Boolean,
        val suBinary: Boolean,
        val suCommand: Boolean,
        val privilegedSuAccess: Boolean,
        val rootDaemonProcess: Boolean,

        /**
         * Supporting / diagnostic checks.
         */
        val dangerousBuildTags: Boolean,
        val writableSystemDirectories: Boolean,
        val dangerousProperties: Boolean,
        val rootFiles: Boolean,
        val rwSystemMounts: Boolean,
    )

    data class Result(
        val isRooted: Boolean,
        val reasons: List<String>,
        val diagnosticReasons: List<String>,
        val checks: Checks,
    )

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    fun getResult(
        packageManager: PackageManager,
    ): Result {
        return try {
            val checks = Checks(
                rootManagementApp =
                    checkRootManagementApps(packageManager),

                suBinary =
                    checkSuBinary(),

                suCommand =
                    checkSuCommand(),

                privilegedSuAccess =
                    checkPrivilegedSuAccess(),

                rootDaemonProcess =
                    checkRootDaemonProcess(),

                dangerousBuildTags =
                    checkDangerousBuildTags(),

                writableSystemDirectories =
                    checkWritableSystemDirectories(),

                dangerousProperties =
                    checkDangerousProperties(),

                rootFiles =
                    checkRootFiles(),

                rwSystemMounts =
                    checkRwSystemMounts(),
            )

            val reasons = checks.getRootReasons()
            val diagnosticReasons = checks.getDiagnosticReasons()

            /*
             * Strong root evidence.
             *
             * IMPORTANT:
             *
             * suBinary and suCommand are intentionally included because
             * a real su executable is a strong root indicator.
             *
             * privilegedSuAccess is stronger because it proves that
             * the application can actually obtain uid=0.
             */
            val isRooted =
                checks.privilegedSuAccess ||
                    checks.rootManagementApp ||
                    checks.suBinary ||
                    checks.suCommand ||
                    checks.rootDaemonProcess

            Log.d(
                TAG,
                """
                Root detection result:
                isRooted=$isRooted

                Strong checks:
                rootManagementApp=${checks.rootManagementApp}
                suBinary=${checks.suBinary}
                suCommand=${checks.suCommand}
                privilegedSuAccess=${checks.privilegedSuAccess}
                rootDaemonProcess=${checks.rootDaemonProcess}

                Diagnostic checks:
                dangerousBuildTags=${checks.dangerousBuildTags}
                dangerousProperties=${checks.dangerousProperties}
                rootFiles=${checks.rootFiles}
                rwSystemMounts=${checks.rwSystemMounts}
                writableSystemDirectories=${checks.writableSystemDirectories}

                reasons=$reasons
                diagnosticReasons=$diagnosticReasons
                """.trimIndent(),
            )

            Result(
                isRooted = isRooted,
                reasons = reasons,
                diagnosticReasons = diagnosticReasons,
                checks = checks,
            )
        } catch (e: Exception) {
            throw ReactNativeDeviceSecurityException(
                "Failed to detect root status.",
                e,
            )
        }
    }

    fun isRooted(
        packageManager: PackageManager,
    ): Boolean {
        return getResult(packageManager).isRooted
    }

    // -------------------------------------------------------------------------
    // Root reasons
    // -------------------------------------------------------------------------

    private fun Checks.getRootReasons(): List<String> {
        return buildList {

            if (rootManagementApp) {
                add("ROOT_MANAGEMENT_APP_DETECTED")
            }

            if (suBinary) {
                add("SU_BINARY_DETECTED")
            }

            if (suCommand) {
                add("SU_COMMAND_AVAILABLE")
            }

            if (privilegedSuAccess) {
                add("PRIVILEGED_SU_ACCESS_DETECTED")
            }

            if (rootDaemonProcess) {
                add("ROOT_DAEMON_PROCESS_DETECTED")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Diagnostic reasons
    // -------------------------------------------------------------------------

    private fun Checks.getDiagnosticReasons(): List<String> {
        return buildList {

            if (dangerousBuildTags) {
                add("DANGEROUS_BUILD_TAGS_DETECTED")
            }

            if (dangerousProperties) {
                add("DANGEROUS_SYSTEM_PROPERTY_DETECTED")
            }

            if (rootFiles) {
                add("ROOT_FILE_DETECTED")
            }

            if (rwSystemMounts) {
                add("RW_SYSTEM_MOUNT_DETECTED")
            }

            if (writableSystemDirectories) {
                add("WRITABLE_SYSTEM_DIRECTORY_DETECTED")
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
            // SuperSU
            "eu.chainfire.supersu",

            // Superuser
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",

            // Magisk
            "com.topjohnwu.magisk",
            "com.topjohnwu.magisk.debug",
            "io.github.vvb2060.magisk",

            // KernelSU
            "me.weishu.kernelsu",
            "com.rifsxd.ksunext",
        )

        return packages.any { packageName ->
            isPackageInstalled(
                packageManager,
                packageName,
            )
        }
    }

    private fun isPackageInstalled(
        packageManager: PackageManager,
        packageName: String,
    ): Boolean {
        return try {
            packageManager.getPackageInfo(
                packageName,
                0,
            )

            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------------------
    // Build tags
    // -------------------------------------------------------------------------

    private fun checkDangerousBuildTags(): Boolean {
        val tags = Build.TAGS ?: return false

        return tags
            .split(",")
            .any { tag ->
                tag.trim().equals(
                    "test-keys",
                    ignoreCase = true,
                )
            }
    }

    // -------------------------------------------------------------------------
    // su binary
    // -------------------------------------------------------------------------

    /**
     * Checks multiple known su locations.
     *
     * We don't rely only on File.exists().
     *
     * We also inspect:
     * - exists
     * - canExecute
     * - file length
     *
     * This gives us more information when debugging filesystem
     * visibility/mount namespace differences.
     */
    private fun checkSuBinary(): Boolean {
        var found = false

        suPaths.forEach { path ->
            val result = inspectSuBinary(path)

            Log.d(
                TAG,
                "suBinary path=$path " +
                    "exists=${result.exists} " +
                    "executable=${result.executable} " +
                    "length=${result.length}",
            )

            if (
                result.exists &&
                (
                    result.executable ||
                        result.length > 0L
                    )
            ) {
                found = true
            }
        }

        return found
    }

    private data class SuBinaryInfo(
        val exists: Boolean,
        val executable: Boolean,
        val length: Long,
    )

    private fun inspectSuBinary(
        path: String,
    ): SuBinaryInfo {
        return try {
            val file = File(path)

            SuBinaryInfo(
                exists = file.exists(),
                executable = file.canExecute(),
                length =
                    if (file.exists()) {
                        file.length()
                    } else {
                        0L
                    },
            )
        } catch (_: SecurityException) {
            SuBinaryInfo(
                exists = false,
                executable = false,
                length = 0L,
            )
        } catch (_: Exception) {
            SuBinaryInfo(
                exists = false,
                executable = false,
                length = 0L,
            )
        }
    }

    // -------------------------------------------------------------------------
    // su command
    // -------------------------------------------------------------------------

    /**
     * Detect su using multiple methods.
     *
     * Method 1:
     *     command -v su
     *
     * Method 2:
     *     which su
     *
     * Method 3:
     *     direct filesystem checks
     *
     * Method 4:
     *     shell executable checks against all known paths.
     */
    private fun checkSuCommand(): Boolean {

        // -------------------------------------------------------------
        // Method 1: command -v
        // -------------------------------------------------------------

        val commandV =
            executeCommand(
                arrayOf(
                    "sh",
                    "-c",
                    "command -v su",
                ),
            )

        Log.d(
            TAG,
            "command -v su: " +
                "exit=${commandV.exitCode}, " +
                "stdout=${commandV.output.trim()}, " +
                "stderr=${commandV.error.trim()}",
        )

        if (
            commandV.exitCode == 0 &&
            commandV.output.trim().isNotEmpty()
        ) {
            return true
        }

        // -------------------------------------------------------------
        // Method 2: which
        // -------------------------------------------------------------

        val which =
            executeCommand(
                arrayOf(
                    "sh",
                    "-c",
                    "which su",
                ),
            )

        Log.d(
            TAG,
            "which su: " +
                "exit=${which.exitCode}, " +
                "stdout=${which.output.trim()}, " +
                "stderr=${which.error.trim()}",
        )

        if (
            which.exitCode == 0 &&
            which.output.trim().isNotEmpty()
        ) {
            return true
        }

        // -------------------------------------------------------------
        // Method 3: direct filesystem check
        // -------------------------------------------------------------

        if (
            suPaths.any { path ->
                inspectSuBinary(path).exists
            }
        ) {
            Log.d(
                TAG,
                "suCommand: found su through direct filesystem check",
            )

            return true
        }

        // -------------------------------------------------------------
        // Method 4: shell executable check
        // -------------------------------------------------------------

        val checks =
            suPaths.joinToString(" || ") { path ->
                "[ -x \"$path\" ] && echo \"$path\""
            }

        val shellResult =
            executeCommand(
                arrayOf(
                    "sh",
                    "-c",
                    checks,
                ),
            )

        Log.d(
            TAG,
            "shell su path check: " +
                "exit=${shellResult.exitCode}, " +
                "stdout=${shellResult.output.trim()}, " +
                "stderr=${shellResult.error.trim()}",
        )

        return shellResult.exitCode == 0 &&
            shellResult.output
                .lineSequence()
                .any { line ->
                    line.trim().isNotEmpty()
                }
    }

    // -------------------------------------------------------------------------
    // Privileged su access
    // -------------------------------------------------------------------------

    /**
     * Attempts multiple su invocation styles.
     *
     * Different su implementations can support different syntax.
     *
     * We therefore try:
     *
     *     su -c id
     *     su -c whoami
     *
     * and the same commands against explicit su paths.
     *
     * We additionally try:
     *
     *     su 0 id
     *     su 0 whoami
     *
     * because some implementations interpret the first argument
     * as the target UID rather than supporting -c.
     */
    private fun checkPrivilegedSuAccess(): Boolean {

        val commands =
            mutableListOf<Array<String>>()

        // -------------------------------------------------------------
        // PATH based su
        // -------------------------------------------------------------

        commands += arrayOf(
            "su",
            "-c",
            "id",
        )

        commands += arrayOf(
            "su",
            "-c",
            "whoami",
        )

        commands += arrayOf(
            "su",
            "0",
            "id",
        )

        commands += arrayOf(
            "su",
            "0",
            "whoami",
        )

        // -------------------------------------------------------------
        // Explicit su paths
        // -------------------------------------------------------------

        suPaths
            .filter { path ->
                inspectSuBinary(path).exists
            }
            .forEach { path ->

                commands += arrayOf(
                    path,
                    "-c",
                    "id",
                )

                commands += arrayOf(
                    path,
                    "-c",
                    "whoami",
                )

                commands += arrayOf(
                    path,
                    "0",
                    "id",
                )

                commands += arrayOf(
                    path,
                    "0",
                    "whoami",
                )
            }

        // -------------------------------------------------------------
        // Execute all methods
        // -------------------------------------------------------------

        commands.forEach { command ->

            val result =
                executeCommand(command)

            Log.d(
                TAG,
                "su attempt: " +
                    "command=${command.joinToString(" ")} " +
                    "exit=${result.exitCode} " +
                    "stdout=${result.output.trim()} " +
                    "stderr=${result.error.trim()}",
            )

            if (isRootIdentity(result)) {
                Log.d(
                    TAG,
                    "PRIVILEGED SU ACCESS DETECTED",
                )

                return true
            }
        }

        return false
    }

    /**
     * Determines whether command output proves root identity.
     */
    private fun isRootIdentity(
        result: CommandResult,
    ): Boolean {

        if (result.exitCode != 0) {
            return false
        }

        val output =
            result.output
                .lowercase()
                .trim()

        if (output.isEmpty()) {
            return false
        }

        /*
         * id output:
         *
         * uid=0(root) gid=0(root)
         *
         * or:
         *
         * uid=0 gid=0
         */
        if (
            Regex(
                """\buid=0(?:\(|\s|$)""",
            ).containsMatchIn(output)
        ) {
            return true
        }

        /*
         * whoami output.
         */
        return output
            .lineSequence()
            .any { line ->
                line.trim() == "root"
            }
    }

    // -------------------------------------------------------------------------
    // Root daemon / process detection
    // -------------------------------------------------------------------------

    private fun checkRootDaemonProcess(): Boolean {
        val result =
            executeCommand(
                arrayOf(
                    "sh",
                    "-c",
                    "ps",
                ),
            )

        Log.d(
            TAG,
            "ps: " +
                "exit=${result.exitCode}, " +
                "stderr=${result.error.trim()}",
        )

        if (result.exitCode != 0) {
            return false
        }

        val output =
            result.output.lowercase()

        val suspiciousProcesses =
            listOf(
                "daemonsu",
                "supersu",
                "magiskd",
                "magiskinit",
                "ksud",
                "ksu",
            )

        return suspiciousProcesses.any { processName ->
            output
                .lineSequence()
                .any { line ->
                    containsProcessName(
                        line = line,
                        processName = processName,
                    )
                }
        }
    }

    private fun containsProcessName(
        line: String,
        processName: String,
    ): Boolean {
        val normalized =
            line
                .trim()
                .lowercase()

        if (normalized.isEmpty()) {
            return false
        }

        return normalized
            .split(Regex("\\s+"))
            .any { token ->
                token == processName ||
                    token.endsWith("/$processName") ||
                    token.endsWith(":$processName")
            }
    }

    // -------------------------------------------------------------------------
    // Writable system directories
    // -------------------------------------------------------------------------

    private fun checkWritableSystemDirectories(): Boolean {
        val directories =
            listOf(
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
                val file =
                    File(path)

                file.exists() &&
                    file.canWrite()
            } catch (_: Exception) {
                false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Dangerous system properties
    // -------------------------------------------------------------------------

    private fun checkDangerousProperties(): Boolean {
        return try {

            val debuggable =
                getSystemProperty(
                    "ro.debuggable",
                )

            val secure =
                getSystemProperty(
                    "ro.secure",
                )

            val buildType =
                getSystemProperty(
                    "ro.build.type",
                )

            Log.d(
                TAG,
                "properties: " +
                    "ro.debuggable=$debuggable " +
                    "ro.secure=$secure " +
                    "ro.build.type=$buildType",
            )

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
        val paths =
            listOf(

                // Magisk / KernelSU artifacts
                "/data/adb/magisk",
                "/data/adb/ksu",
                "/data/adb/ksud",

                // Legacy root artifacts
                "/cache/su",
                "/dev/com.koushikdutta.superuser.daemon/",
                "/system/app/Superuser.apk",
                "/system/app/SuperSU.apk",
                "/system/etc/init.d",
                "/system/xbin/daemonsu",

                // Other known root binaries
                "/system/xbin/busybox",
                "/system/bin/failsafe/su",
                "/system/sd/xbin/su",
            )

        return paths.any { path ->
            fileExists(path)
        }
    }

    // -------------------------------------------------------------------------
    // RW system mounts
    // -------------------------------------------------------------------------

    private fun checkRwSystemMounts(): Boolean {
        val result =
            executeCommand(
                command = arrayOf(
                    "mount",
                ),
            )

        if (result.exitCode != 0) {
            return false
        }

        return result.output
            .lineSequence()
            .any { line ->
                isSuspiciousRwSystemMount(line)
            }
    }

    private fun isSuspiciousRwSystemMount(
        line: String,
    ): Boolean {
        val normalized =
            line
                .lowercase()
                .trim()

        if (normalized.isEmpty()) {
            return false
        }

        val mountPoint =
            extractMountPoint(normalized)

        if (
            mountPoint != "/system" &&
            mountPoint != "/vendor" &&
            mountPoint != "/product" &&
            mountPoint != "/odm"
        ) {
            return false
        }

        val options =
            extractMountOptions(normalized)

        return options
            .split(",")
            .any { option ->
                option.trim() == "rw"
            }
    }

    private fun extractMountPoint(
        line: String,
    ): String {

        val onIndex =
            line.indexOf(" on ")

        if (onIndex >= 0) {

            val afterOn =
                line.substring(
                    onIndex + 4,
                )

            val typeIndex =
                afterOn.indexOf(" type ")

            if (typeIndex >= 0) {

                return afterOn
                    .substring(
                        0,
                        typeIndex,
                    )
                    .trim()
            }
        }

        val knownMountPoints =
            listOf(
                "/system",
                "/vendor",
                "/product",
                "/odm",
            )

        return knownMountPoints
            .firstOrNull { mountPoint ->
                Regex(
                    pattern =
                        "(^|\\s)" +
                            Regex.escape(
                                mountPoint,
                            ) +
                            "(\\s|$)",
                ).containsMatchIn(line)
            }
            .orEmpty()
    }

    private fun extractMountOptions(
        line: String,
    ): String {

        val openParen =
            line.lastIndexOf("(")

        val closeParen =
            line.lastIndexOf(")")

        if (
            openParen >= 0 &&
            closeParen > openParen
        ) {
            return line.substring(
                openParen + 1,
                closeParen,
            )
        }

        return ""
    }

    // -------------------------------------------------------------------------
    // System properties
    // -------------------------------------------------------------------------

    private fun getSystemProperty(
        property: String,
    ): String {

        val result =
            executeCommand(
                command = arrayOf(
                    "getprop",
                    property,
                ),
            )

        return if (result.exitCode == 0) {
            result.output
                .lineSequence()
                .firstOrNull()
                ?.trim()
                .orEmpty()
        } else {
            ""
        }
    }

    // -------------------------------------------------------------------------
    // Generic command execution
    // -------------------------------------------------------------------------

    private data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String,
    )

    /**
     * Execute command with:
     *
     * - stdout capture
     * - stderr capture
     * - timeout
     * - process cleanup
     *
     * Capturing stderr is important because su implementations frequently
     * report failures there.
     */
    private fun executeCommand(
        command: Array<String>,
    ): CommandResult {

        var process: Process? = null

        return try {

            process =
                Runtime.getRuntime().exec(
                    command,
                )

            val stdout =
                StringBuilder()

            val stderr =
                StringBuilder()

            val stdoutLatch =
                CountDownLatch(1)

            val stderrLatch =
                CountDownLatch(1)

            // -------------------------------------------------------------
            // stdout
            // -------------------------------------------------------------

            val stdoutThread =
                Thread {

                    try {

                        BufferedReader(
                            InputStreamReader(
                                process.inputStream,
                            ),
                        ).use { reader ->

                            var line: String?

                            while (
                                reader
                                    .readLine()
                                    .also {
                                        line = it
                                    } != null
                            ) {
                                stdout
                                    .append(line)
                                    .append('\n')
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore.
                    } finally {
                        stdoutLatch.countDown()
                    }
                }

            // -------------------------------------------------------------
            // stderr
            // -------------------------------------------------------------

            val stderrThread =
                Thread {

                    try {

                        BufferedReader(
                            InputStreamReader(
                                process.errorStream,
                            ),
                        ).use { reader ->

                            var line: String?

                            while (
                                reader
                                    .readLine()
                                    .also {
                                        line = it
                                    } != null
                            ) {
                                stderr
                                    .append(line)
                                    .append('\n')
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore.
                    } finally {
                        stderrLatch.countDown()
                    }
                }

            stdoutThread.isDaemon = true
            stderrThread.isDaemon = true

            stdoutThread.start()
            stderrThread.start()

            // -------------------------------------------------------------
            // Wait
            // -------------------------------------------------------------

            val finished =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                ) {

                    process.waitFor(
                        COMMAND_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS,
                    )
                } else {

                    stdoutLatch.await(
                        COMMAND_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS,
                    )

                    !process.isAlive
                }

            // -------------------------------------------------------------
            // Timeout
            // -------------------------------------------------------------

            if (!finished) {

                try {
                    process.destroy()
                } catch (_: Exception) {
                    // Ignore.
                }

                return CommandResult(
                    exitCode = -1,
                    output = stdout.toString(),
                    error = stderr.toString(),
                )
            }

            // -------------------------------------------------------------
            // Give readers time to finish
            // -------------------------------------------------------------

            stdoutLatch.await(
                100,
                TimeUnit.MILLISECONDS,
            )

            stderrLatch.await(
                100,
                TimeUnit.MILLISECONDS,
            )

            CommandResult(
                exitCode =
                    try {
                        process.exitValue()
                    } catch (_: Exception) {
                        -1
                    },
                output =
                    stdout.toString(),
                error =
                    stderr.toString(),
            )
        } catch (e: Exception) {

            CommandResult(
                exitCode = -1,
                output = "",
                error = e.message.orEmpty(),
            )
        } finally {

            try {
                process?.destroy()
            } catch (_: Exception) {
                // Ignore.
            }
        }
    }

    // -------------------------------------------------------------------------
    // File helpers
    // -------------------------------------------------------------------------

    private fun fileExists(
        path: String,
    ): Boolean {
        return try {
            File(path).exists()
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}