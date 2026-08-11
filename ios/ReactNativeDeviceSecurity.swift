import Foundation
import UIKit

@objc(ReactNativeDeviceSecurity)
public class ReactNativeDeviceSecurity: NSObject {

    @objc
    public func getSecurityStatus(
        _ resolve: @escaping RCTPromiseResolveBlock,
        rejecter reject: @escaping RCTPromiseRejectBlock
    ) {
        do {
            let isJailbroken = JailbreakDetection.isJailbroken()
            let isEmulator = EmulatorDetection.isEmulator()

            let status: [String: Any] = [
                "isCompromised": isJailbroken || isEmulator,
                "isRooted": false,
                "isJailbroken": isJailbroken,
                "isEmulator": isEmulator
            ]

            resolve(status)
        } catch {
            let securityError = ReactNativeDeviceSecurityError.detectionFailed

            reject(
                "DEVICE_SECURITY_DETECTION_FAILED",
                securityError.localizedDescription,
                error
            )
        }
    }
}