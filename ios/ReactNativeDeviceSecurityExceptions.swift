import Foundation

enum ReactNativeDeviceSecurityError: Error {
    case unableToDetermineSecurityStatus
    case invalidSecurityStatus
}

extension ReactNativeDeviceSecurityError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .unableToDetermineSecurityStatus:
            return "Unable to determine device security status."

        case .invalidSecurityStatus:
            return "Invalid device security status."
        }
    }
}