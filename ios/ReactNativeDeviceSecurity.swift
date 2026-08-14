// ReactNativeDeviceSecurity.swift

import Foundation
import UIKit

@objc(ReactNativeDeviceSecurity)
class ReactNativeDeviceSecurity: NSObject {

  // MARK: - Security Status

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
      rejectDetectionFailure(
        reject: reject,
        error: error
      )
    }
  }

  // MARK: - Root

  @objc(isRooted:rejecter:)
  func isRooted(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    // Root detection is Android-specific.
    resolve(false)
  }

  // MARK: - Jailbreak

  @objc(isJailbroken:rejecter:)
  func isJailbroken(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    do {
      resolve(
        JailbreakDetection.isJailbroken()
      )
    } catch {
      rejectDetectionFailure(
        reject: reject,
        error: error
      )
    }
  }

  // MARK: - Emulator

  @objc(isEmulator:rejecter:)
  func isEmulator(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    do {
      resolve(
        EmulatorDetection.isEmulator()
      )
    } catch {
      rejectDetectionFailure(
        reject: reject,
        error: error
      )
    }
  }

  // MARK: - Compromised

  @objc(isSecurityCompromised:rejecter:)
  func isSecurityCompromised(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    do {
      let isJailbroken =
        JailbreakDetection.isJailbroken()

      let isEmulator =
        EmulatorDetection.isEmulator()

      resolve(
        isJailbroken || isEmulator
      )
    } catch {
      rejectDetectionFailure(
        reject: reject,
        error: error
      )
    }
  }

  // MARK: - Root Detection Result

  @objc(getRootDetectionResult:rejecter:)
  func getRootDetectionResult(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    // Root detection is Android-specific.
    //
    // We still return the complete cross-platform shape
    // so consumers don't need platform-specific branching.
    resolve([
      "isRooted": false,
      "checks": [
        "rootManagementApp": false,
        "dangerousBuildTags": false,
        "suBinary": false,
        "suCommand": false,
        "writableSystemDirectories": false,
        "dangerousProperties": false,
        "rootFiles": false,
        "rwSystemMounts": false
      ]
    ])
  }

  // MARK: - Error Handling

  private func rejectDetectionFailure(
    reject: @escaping RCTPromiseRejectBlock,
    error: Error
  ) {
    let securityError =
      ReactNativeDeviceSecurityError.detectionFailed

    reject(
      "DEVICE_SECURITY_DETECTION_FAILED",
      securityError.localizedDescription,
      error
    )
  }
}