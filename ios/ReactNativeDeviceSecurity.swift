// ReactNativeDeviceSecurity.swift

import Foundation
import UIKit

@objc(ReactNativeDeviceSecurity)
class ReactNativeDeviceSecurity: NSObject {

  @objc(getSecurityStatus:rejecter:)
  func getSecurityStatus(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    do {
      let isJailbroken = JailbreakDetection.isJailbroken()
      let isEmulator = EmulatorDetection.isEmulator()

      let status: [String: Any] = [
        "isCompromised": isJailbroken,
        "isRooted": false,
        "isJailbroken": isJailbroken,
        "isEmulator": isEmulator
      ]

      resolve(status)
    } catch {
      let securityError =
        ReactNativeDeviceSecurityError.detectionFailed

      reject(
        "DEVICE_SECURITY_DETECTION_FAILED",
        securityError.localizedDescription,
        error
      )
    }
  }
}