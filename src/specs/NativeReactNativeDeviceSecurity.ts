// NativeReactNativeDeviceSecurity.ts

import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export type RootDetectionChecks = {
  rootManagementApp: boolean;
  dangerousBuildTags: boolean;
  suBinary: boolean;
  suCommand: boolean;
  writableSystemDirectories: boolean;
  dangerousProperties: boolean;
  rootFiles: boolean;
  rwSystemMounts: boolean;
};

export type RootDetectionResult = {
  isRooted: boolean;
  checks: RootDetectionChecks;
};

export type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;
};

export interface Spec extends TurboModule {
  getSecurityStatus(): Promise<DeviceSecurityStatus>;
  isRooted(): Promise<boolean>;
  isJailbroken(): Promise<boolean>;
  isEmulator(): Promise<boolean>;
  isSecurityCompromised(): Promise<boolean>;
  getRootDetectionResult(): Promise<RootDetectionResult>;
}

const NativeReactNativeDeviceSecurity =
  TurboModuleRegistry.getEnforcing<Spec>(
    'ReactNativeDeviceSecurity',
  );

export default NativeReactNativeDeviceSecurity;