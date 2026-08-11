
import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';

export type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;
};

export interface Spec extends TurboModule {
  getSecurityStatus(): Promise<DeviceSecurityStatus>;
}

const NativeReactNativeDeviceSecurity =
  TurboModuleRegistry.getEnforcing<Spec>('ReactNativeDeviceSecurity');

export default NativeReactNativeDeviceSecurity;
