// src/specs/index.ts

import NativeReactNativeDeviceSecurity, {
  type DeviceSecurityStatus,
} from './specs/NativeReactNativeDeviceSecurity';

export type {DeviceSecurityStatus};

export function getSecurityStatus(): Promise<DeviceSecurityStatus> {
  return NativeReactNativeDeviceSecurity.getSecurityStatus();
}

export default {
  getSecurityStatus,
};