// JailbreakDetection.swift

import Foundation
import UIKit

enum JailbreakDetection {

  static func isJailbroken() -> Bool {
    #if targetEnvironment(simulator)
      return false
    #endif

    if canAccessCydia() {
      return true
    }

    if hasSuspiciousFiles() {
      return true
    }

    if canWriteOutsideSandbox() {
      return true
    }

    return false
  }

  private static func canAccessCydia() -> Bool {
    guard let url = URL(string: "cydia://package/com.example.package") else {
      return false
    }

    return UIApplication.shared.canOpenURL(url)
  }

  private static func hasSuspiciousFiles() -> Bool {
    let suspiciousPaths = [
      "/Applications/Cydia.app",
      "/Library/MobileSubstrate/MobileSubstrate.dylib",
      "/bin/bash",
      "/usr/sbin/sshd",
      "/etc/apt",
      "/private/var/lib/apt/"
    ]

    return suspiciousPaths.contains {
      FileManager.default.fileExists(atPath: $0)
    }
  }

  private static func canWriteOutsideSandbox() -> Bool {
    let testPath = "/private/device-security-test.txt"

    do {
      try "test".write(
        toFile: testPath,
        atomically: true,
        encoding: .utf8
      )

      try? FileManager.default.removeItem(atPath: testPath)
      return true
    } catch {
      return false
    }
  }
}