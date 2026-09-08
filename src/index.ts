// src/index.ts

import NativeReactNativeDeviceSecurity, {
  type DeviceSecurityStatus,
  type RootDetectionChecks,
  type RootDetectionResult,
  type SecurityCheckResult,
} from "./specs/NativeReactNativeDeviceSecurity";
export type {
  DeviceSecurityStatus,
  RootDetectionChecks,
  RootDetectionResult,
  SecurityCheckResult,
};
export function getSecurityStatus(): Promise<DeviceSecurityStatus> {
  return NativeReactNativeDeviceSecurity.getSecurityStatus();
}
export function isRooted(): Promise<SecurityCheckResult> {
  return NativeReactNativeDeviceSecurity.isRooted();
}
export function isJailbroken(): Promise<SecurityCheckResult> {
  return NativeReactNativeDeviceSecurity.isJailbroken();
}
export function isEmulator(): Promise<SecurityCheckResult> {
  return NativeReactNativeDeviceSecurity.isEmulator();
}
export function isSecurityCompromised(): Promise<SecurityCheckResult> {
  return NativeReactNativeDeviceSecurity.isSecurityCompromised();
}
export function getRootDetectionResult(): Promise<RootDetectionResult> {
  return NativeReactNativeDeviceSecurity.getRootDetectionResult();
}
export default {
  getSecurityStatus,
  isRooted,
  isJailbroken,
  isEmulator,
  isSecurityCompromised,
  getRootDetectionResult,
};
