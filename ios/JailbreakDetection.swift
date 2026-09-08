// JailbreakDetection.swift

import Foundation
import UIKit

struct SecurityCheckResult {
  let detected: Bool
  let reasons: [String]
}

enum JailbreakDetection {

  // MARK: - Public

  static func detect() -> SecurityCheckResult {
    #if targetEnvironment(simulator)

      // Never report the iOS simulator as jailbroken.
      return SecurityCheckResult(
        detected: false,
        reasons: []
      )

    #else

      var reasons: [String] = []

      reasons.append(contentsOf: suspiciousFileReasons())

      if canWriteOutsideSandbox() {
        reasons.append("SANDBOX_ESCAPE")
      }

      if canAccessSuspiciousURLScheme() {
        reasons.append("SUSPICIOUS_URL_SCHEME")
      }

      reasons.append(contentsOf: suspiciousEnvironmentReasons())

      let uniqueReasons = Array(
        Set(reasons)
      ).sorted()

      return SecurityCheckResult(
        detected: !uniqueReasons.isEmpty,
        reasons: uniqueReasons
      )

    #endif
  }

  // MARK: - Suspicious Files

  private static func suspiciousFileReasons() -> [String] {
    let suspiciousFiles: [(path: String, reason: String)] = [

      // Jailbreak/package managers
      (
        "/Applications/Cydia.app",
        "CYDIA_INSTALLED"
      ),
      (
        "/Applications/Sileo.app",
        "SILEO_INSTALLED"
      ),
      (
        "/Applications/Zebra.app",
        "ZEBRA_INSTALLED"
      ),
      (
        "/Applications/FakeCarrier.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/Icy.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/IntelliScreen.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/MxTube.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/RockApp.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/SBSettings.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/WinterBoard.app",
        "JAILBREAK_APP_DETECTED"
      ),
      (
        "/Applications/blackra1n.app",
        "JAILBREAK_APP_DETECTED"
      ),

      // MobileSubstrate
      (
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "MOBILE_SUBSTRATE"
      ),
      (
        "/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
        "MOBILE_SUBSTRATE"
      ),
      (
        "/Library/MobileSubstrate/DynamicLibraries/Veency.plist",
        "MOBILE_SUBSTRATE"
      ),

      // Package management
      (
        "/etc/apt",
        "APT_ARTIFACT"
      ),
      (
        "/private/etc/apt",
        "APT_ARTIFACT"
      ),
      (
        "/private/var/lib/apt",
        "APT_ARTIFACT"
      ),
      (
        "/private/var/lib/cydia",
        "CYDIA_ARTIFACT"
      ),
      (
        "/var/cache/apt",
        "APT_ARTIFACT"
      ),
      (
        "/var/lib/apt",
        "APT_ARTIFACT"
      ),
      (
        "/var/lib/cydia",
        "CYDIA_ARTIFACT"
      ),

      // Jailbreak-specific directories/files
      (
        "/private/var/stash",
        "JAILBREAK_STASH"
      ),
      (
        "/private/var/tmp/cydia.log",
        "CYDIA_ARTIFACT"
      ),
      (
        "/var/tmp/cydia.log",
        "CYDIA_ARTIFACT"
      ),

      // Dynamic analysis tools
      (
        "/usr/sbin/frida-server",
        "FRIDA_SERVER"
      ),
      (
        "/usr/bin/cycript",
        "CYCRIPT"
      ),
      (
        "/usr/local/bin/cycript",
        "CYCRIPT"
      ),
      (
        "/usr/lib/libcycript.dylib",
        "CYCRIPT"
      ),

      // Jailbreak launch daemons
      (
        "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
        "JAILBREAK_LAUNCH_DAEMON"
      ),
      (
        "/System/Library/LaunchDaemons/com.ikey.bbot.plist",
        "JAILBREAK_LAUNCH_DAEMON"
      )
    ]

    var reasons: [String] = []

    for item in suspiciousFiles {
      if FileManager.default.fileExists(atPath: item.path) {
        reasons.append(item.reason)
      }
    }

    return reasons
  }

  // MARK: - Sandbox Escape

  private static func canWriteOutsideSandbox() -> Bool {
    let testPaths = [
      "/private/device-security-test.txt",
      "/private/jailbreak-test.txt"
    ]

    for path in testPaths {
      do {
        try "device-security-test".write(
          toFile: path,
          atomically: true,
          encoding: .utf8
        )

        try? FileManager.default.removeItem(
          atPath: path
        )

        return true

      } catch {
        continue
      }
    }

    return false
  }

  // MARK: - URL Schemes

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

  // MARK: - Environment

  private static func suspiciousEnvironmentReasons() -> [String] {
    let suspiciousVariables = [
      "DYLD_INSERT_LIBRARIES",
      "DYLD_FRAMEWORK_PATH",
      "DYLD_LIBRARY_PATH"
    ]

    let environment =
      ProcessInfo.processInfo.environment

    var reasons: [String] = []

    for variable in suspiciousVariables {
      guard let value = environment[variable],
            !value.isEmpty else {
        continue
      }

      switch variable {
      case "DYLD_INSERT_LIBRARIES":
        reasons.append("DYLD_INSERT_LIBRARIES")

      case "DYLD_FRAMEWORK_PATH":
        reasons.append("DYLD_FRAMEWORK_PATH")

      case "DYLD_LIBRARY_PATH":
        reasons.append("DYLD_LIBRARY_PATH")

      default:
        break
      }
    }

    return reasons
  }
}