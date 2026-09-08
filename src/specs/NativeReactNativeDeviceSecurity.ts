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
  reasons: string[];
  checks: RootDetectionChecks;
};

export type SecurityCheckResult = {
  detected: boolean;
  reasons: string[];
};

export type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;

  rootReasons: string[];
  jailbreakReasons: string[];
  emulatorReasons: string[];
  compromiseReasons: string[];
};

export interface Spec extends TurboModule {
  getSecurityStatus(): Promise<DeviceSecurityStatus>;

  isRooted(): Promise<SecurityCheckResult>;

  isJailbroken(): Promise<SecurityCheckResult>;

  isEmulator(): Promise<SecurityCheckResult>;

  isSecurityCompromised(): Promise<SecurityCheckResult>;

  getRootDetectionResult(): Promise<RootDetectionResult>;
}

const NativeReactNativeDeviceSecurity =
  TurboModuleRegistry.getEnforcing<Spec>(
    'ReactNativeDeviceSecurity',
  );

export default NativeReactNativeDeviceSecurity;