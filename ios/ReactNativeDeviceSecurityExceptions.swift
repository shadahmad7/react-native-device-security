// ReactNativeDeviceSecurityExceptions.swift

import Foundation

enum ReactNativeDeviceSecurityError: Error {
  case detectionFailed
}

extension ReactNativeDeviceSecurityError: LocalizedError {
  var errorDescription: String? {
    switch self {
    case .detectionFailed:
      return "Unable to determine device security status."
    }
  }
}