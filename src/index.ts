// src/index.ts

import NativeReactNativeDeviceSecurity, {
  type DeviceSecurityStatus,
  type RootDetectionChecks,
  type RootDetectionResult,
} from './specs/NativeReactNativeDeviceSecurity';

export type {
  DeviceSecurityStatus,
  RootDetectionChecks,
  RootDetectionResult,
};

export function getSecurityStatus(): Promise<DeviceSecurityStatus> {
  return NativeReactNativeDeviceSecurity.getSecurityStatus();
}

export function isRooted(): Promise<boolean> {
  return NativeReactNativeDeviceSecurity.isRooted();
}

export function isJailbroken(): Promise<boolean> {
  return NativeReactNativeDeviceSecurity.isJailbroken();
}

export function isEmulator(): Promise<boolean> {
  return NativeReactNativeDeviceSecurity.isEmulator();
}

export function isSecurityCompromised(): Promise<boolean> {
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