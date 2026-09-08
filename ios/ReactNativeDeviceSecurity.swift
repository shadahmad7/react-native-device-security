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
    let jailbreakResult =
      JailbreakDetection.detect()

    let isEmulator =
      EmulatorDetection.isEmulator()

    let emulatorReasons: [String] =
      isEmulator
        ? ["EMULATOR_DETECTED"]
        : []

    let compromiseReasons =
      buildCompromiseReasons(
        jailbreakReasons: jailbreakResult.reasons,
        emulatorReasons: emulatorReasons
      )

    let isCompromised =
      jailbreakResult.detected ||
      isEmulator

    let status: [String: Any] = [
      "isCompromised": isCompromised,
      "isRooted": false,
      "isJailbroken": jailbreakResult.detected,
      "isEmulator": isEmulator,

      "rootReasons": [],
      "jailbreakReasons": jailbreakResult.reasons,
      "emulatorReasons": emulatorReasons,
      "compromiseReasons": compromiseReasons
    ]

    resolve(status)
  }

  // MARK: - Root

  @objc(isRooted:rejecter:)
  func isRooted(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    // Root is Android terminology.
    // iOS uses jailbreak detection.

    resolve([
      "detected": false,
      "reasons": []
    ])
  }

  // MARK: - Jailbreak

  @objc(isJailbroken:rejecter:)
  func isJailbroken(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let result =
      JailbreakDetection.detect()

    resolve([
      "detected": result.detected,
      "reasons": result.reasons
    ])
  }

  // MARK: - Emulator

  @objc(isEmulator:rejecter:)
  func isEmulator(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let detected =
      EmulatorDetection.isEmulator()

    let reasons: [String] =
      detected
        ? ["EMULATOR_DETECTED"]
        : []

    resolve([
      "detected": detected,
      "reasons": reasons
    ])
  }

  // MARK: - Compromised

  @objc(isSecurityCompromised:rejecter:)
  func isSecurityCompromised(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let jailbreakResult =
      JailbreakDetection.detect()

    let isEmulator =
      EmulatorDetection.isEmulator()

    let emulatorReasons: [String] =
      isEmulator
        ? ["EMULATOR_DETECTED"]
        : []

    let compromiseReasons =
      buildCompromiseReasons(
        jailbreakReasons: jailbreakResult.reasons,
        emulatorReasons: emulatorReasons
      )

    let isCompromised =
      jailbreakResult.detected ||
      isEmulator

    resolve([
      "detected": isCompromised,
      "reasons": compromiseReasons
    ])
  }

  // MARK: - Root Detection Result

  @objc(getRootDetectionResult:rejecter:)
  func getRootDetectionResult(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    resolve([
      "isRooted": false,
      "reasons": [],
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

  // MARK: - Compromise Reasons

  private func buildCompromiseReasons(
    jailbreakReasons: [String],
    emulatorReasons: [String]
  ) -> [String] {
    return Array(
      Set(
        jailbreakReasons +
        emulatorReasons
      )
    ).sorted()
  }
}