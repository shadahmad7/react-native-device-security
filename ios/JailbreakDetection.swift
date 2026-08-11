import Foundation
import UIKit

enum JailbreakDetection {

    static func isJailbroken() -> Bool {
        #if targetEnvironment(simulator)
        return false
        #else

        return hasJailbreakFiles()
            || canWriteOutsideSandbox()
            || canOpenCydiaURL()
            || hasSuspiciousEnvironmentVariables()

        #endif
    }

    private static func hasJailbreakFiles() -> Bool {
        let suspiciousPaths = [
            "/Applications/Cydia.app",
            "/Applications/Sileo.app",
            "/Applications/Zebra.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/Library/MobileSubstrate/DynamicLibraries",
            "/usr/sbin/sshd",
            "/usr/bin/ssh",
            "/usr/bin/cycript",
            "/etc/apt",
            "/private/var/lib/apt/",
            "/private/var/lib/cydia",
            "/private/var/stash",
            "/var/lib/dpkg",
            "/var/cache/apt",
            "/var/log/syslog"
        ]

        return suspiciousPaths.contains {
            FileManager.default.fileExists(atPath: $0)
        }
    }

    private static func canWriteOutsideSandbox() -> Bool {
        let testPath = "/private/jailbreak_test.txt"

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

    private static func canOpenCydiaURL() -> Bool {
        guard let url = URL(string: "cydia://package/com.example.package") else {
            return false
        }

        return UIApplication.shared.canOpenURL(url)
    }

    private static func hasSuspiciousEnvironmentVariables() -> Bool {
        let environment = ProcessInfo.processInfo.environment

        let suspiciousVariables = [
            "DYLD_INSERT_LIBRARIES"
        ]

        return suspiciousVariables.contains {
            environment[$0] != nil
        }
    }
}