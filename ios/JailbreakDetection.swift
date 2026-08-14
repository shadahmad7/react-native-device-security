// JailbreakDetection.swift

import Foundation
import UIKit

enum JailbreakDetection {

  static func isJailbroken() -> Bool {
    #if targetEnvironment(simulator)
      return false
    #else

      if hasSuspiciousFiles() {
        return true
      }

      if canWriteOutsideSandbox() {
        return true
      }

      if canAccessSuspiciousURLScheme() {
        return true
      }

      if hasSuspiciousEnvironmentVariables() {
        return true
      }

      return false
    #endif
  }

  // MARK: - File Checks

  private static func hasSuspiciousFiles() -> Bool {
    let suspiciousPaths = [
      // Package managers / jailbreak apps
      "/Applications/Cydia.app",
      "/Applications/Sileo.app",
      "/Applications/Zebra.app",
      "/Applications/FakeCarrier.app",
      "/Applications/Icy.app",
      "/Applications/IntelliScreen.app",
      "/Applications/MxTube.app",
      "/Applications/RockApp.app",
      "/Applications/SBSettings.app",
      "/Applications/WinterBoard.app",
      "/Applications/blackra1n.app",

      // MobileSubstrate / injection
      "/Library/MobileSubstrate/MobileSubstrate.dylib",
      "/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
      "/Library/MobileSubstrate/DynamicLibraries/Veency.plist",

      // SSH / shell
      "/bin/bash",
      "/bin/sh",
      "/usr/bin/ssh",
      "/usr/bin/sshd",
      "/usr/sbin/sshd",
      "/etc/ssh/sshd_config",

      // Package managers
      "/etc/apt",
      "/private/etc/apt",
      "/private/var/lib/apt",
      "/private/var/lib/cydia",
      "/var/cache/apt",
      "/var/lib/apt",
      "/var/lib/cydia",

      // Jailbreak-specific files
      "/private/var/stash",
      "/private/var/tmp/cydia.log",
      "/var/tmp/cydia.log",
      "/var/log/syslog",

      // Dynamic analysis / reverse engineering tools
      "/usr/sbin/frida-server",
      "/usr/bin/cycript",
      "/usr/local/bin/cycript",
      "/usr/lib/libcycript.dylib",

      // Launch daemons
      "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
      "/System/Library/LaunchDaemons/com.ikey.bbot.plist",

      // Other common jailbreak artifacts
      "/usr/libexec/sftp-server",
      "/usr/libexec/ssh-keysign"
    ]

    return suspiciousPaths.contains {
      FileManager.default.fileExists(atPath: $0)
    }
  }

  // MARK: - Sandbox Escape

  private static func canWriteOutsideSandbox() -> Bool {
    let testPaths = [
      "/private/jailbreak.txt",
      "/private/device-security-test.txt"
    ]

    for path in testPaths {
      do {
        try "device-security-test".write(
          toFile: path,
          atomically: true,
          encoding: .utf8
        )

        try? FileManager.default.removeItem(atPath: path)

        return true
      } catch {
        continue
      }
    }

    return false
  }

  // MARK: - URL Scheme Checks

  private static func canAccessSuspiciousURLScheme() -> Bool {
    let schemes = [
      "cydia://",
      "sileo://",
      "zebra://",
      "filza://"
    ]

    for scheme in schemes {
      guard let url = URL(string: scheme) else {
        continue
      }

      if UIApplication.shared.canOpenURL(url) {
        return true
      }
    }

    return false
  }

  // MARK: - Dynamic Library / Injection Checks

  private static func hasSuspiciousEnvironmentVariables() -> Bool {
    let suspiciousVariables = [
      "DYLD_INSERT_LIBRARIES",
      "DYLD_FRAMEWORK_PATH",
      "DYLD_LIBRARY_PATH"
    ]

    let environment = ProcessInfo.processInfo.environment

    return suspiciousVariables.contains { variable in
      guard let value = environment[variable] else {
        return false
      }

      return !value.isEmpty
    }
  }
}