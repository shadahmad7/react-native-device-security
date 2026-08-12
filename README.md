# @shadahmad7/react-native-device-security

A lightweight, native **device security detection TurboModule** for React Native CLI applications.

`@shadahmad7/react-native-device-security` provides a single JavaScript API for detecting common indicators of potentially compromised environments on **Android and iOS**.

It performs the checks locally on the device and **does not require a backend or server-side integration**.

> **Security note:** Root/jailbreak detection is a security signal, not a guarantee of device integrity. A sufficiently modified device may bypass detection mechanisms.

## Features

- Native Android implementation using Kotlin
- Native iOS implementation using Swift
- Android root detection
- iOS jailbreak detection
- Android emulator detection
- iOS simulator detection
- Unified security status returned to JavaScript
- No backend integration required
- No Expo dependency
- React Native TurboModule interface
- Designed for React Native New Architecture
- Platform-specific implementation behind one JavaScript API

## Installation

```bash
npm install @shadahmad7/react-native-device-security
```

or:

```bash
yarn add @shadahmad7/react-native-device-security
```

For iOS:

```bash
cd ios
pod install
```

Then rebuild the application.

## Usage

```tsx
import DeviceSecurity from '@shadahmad7/react-native-device-security';

const status = await DeviceSecurity.getSecurityStatus();

console.log(status);
```

Example:

```ts
{
  isCompromised: false,
  isRooted: false,
  isJailbroken: false,
  isEmulator: false
}
```

## API

### `getSecurityStatus()`

```ts
getSecurityStatus(): Promise<DeviceSecurityStatus>
```

The returned object is:

```ts
type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;
};
```

### `isCompromised`

A convenience flag indicating that one of the supported compromise/environment checks has been triggered.

For the current implementation, this can include:

- Android root detection
- iOS jailbreak detection

**Emulator/simulator detection is exposed separately through `isEmulator` and is not included in `isCompromised`.**

Example:

```tsx
const status = await DeviceSecurity.getSecurityStatus();

if (status.isCompromised) {
  // Handle potentially compromised device.
}
```

### `isRooted`

Indicates whether Android root indicators were detected.

On iOS this is always:

```ts
false
```

### `isJailbroken`

Indicates whether iOS jailbreak indicators were detected.

On Android this is always:

```ts
false
```

### `isEmulator`

Indicates whether the application is running in an emulator/simulator environment.

This is useful for testing and for applications that need to distinguish physical devices from virtual environments.

## Platform behavior

### Android

The Android implementation performs native checks for:

- Root indicators
- Root-related files/configuration
- Emulator indicators

Result:

```ts
{
  isCompromised: boolean,
  isRooted: boolean,
  isJailbroken: false,
  isEmulator: boolean
}
```

### iOS

The iOS implementation performs native checks for:

- Jailbreak indicators
- Simulator environment

Result:

```ts
{
  isCompromised: boolean,
  isRooted: false,
  isJailbroken: boolean,
  isEmulator: boolean
}
```

## Recommended application architecture

For an application that needs to use device security throughout the app, it is recommended to call the native module once from a centralized React Context/provider.

Example:

```tsx
import DeviceSecurity, {
  type DeviceSecurityStatus,
} from '@shadahmad7/react-native-device-security';
import React, {useEffect, useMemo, useState} from 'react';

const initialStatus: DeviceSecurityStatus = {
  isCompromised: false,
  isRooted: false,
  isJailbroken: false,
  isEmulator: false,
};

export const DeviceSecurityProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [securityStatus, setSecurityStatus] =
    useState<DeviceSecurityStatus>(initialStatus);

  useEffect(() => {
    DeviceSecurity.getSecurityStatus()
      .then(setSecurityStatus)
      .catch(() => {
        // Handle detection failure according to application policy.
      });
  }, []);

  const value = useMemo(
    () => ({
      securityStatus,
      isCompromised: securityStatus.isCompromised,
    }),
    [securityStatus],
  );

  return (
    <DeviceSecurityContext.Provider value={value}>
      {children}
    </DeviceSecurityContext.Provider>
  );
};
```

This keeps device-security checks in one place instead of calling native detection logic throughout the application.

## Security considerations

This library is intended to provide **local device-security signals**.

It should not be considered a replacement for stronger platform integrity mechanisms.

Root and jailbreak detection can be bypassed by:

- Modified operating systems
- Runtime hooking
- Instrumentation
- Concealment/root-hiding tools
- Reverse engineering
- Changes to detection indicators

Therefore:

- Do not treat `isCompromised === false` as proof that a device is secure.
- Do not rely on a single security check for high-risk operations.
- Use multiple independent signals when appropriate.
- Keep sensitive security decisions out of JavaScript where practical.
- Apply your application's own security policy to the returned status.

The library does not transmit device-security information to a backend.

## Testing

### Android

You can test emulator detection using an Android Emulator.

For root detection, use a physical rooted Android device or an appropriately configured rooted test environment.

A normal non-rooted Android device should return:

```ts
{
  isRooted: false
}
```

An emulator may return:

```ts
{
  isEmulator: true
}
```

Whether root detection succeeds depends on the emulator image and its configuration.

### iOS

You can test simulator detection using the iOS Simulator.

The simulator should report:

```ts
{
  isEmulator: true
}
```

Jailbreak detection requires a jailbroken physical iOS device. A normal physical iPhone cannot be used to simulate a real jailbreak state.

For production validation, test the library on:

- A normal physical Android device
- A normal physical iOS device
- Android Emulator
- iOS Simulator
- Rooted Android test device, when available
- Jailbroken iOS test device, when available

## Architecture

The library exposes one JavaScript API while keeping platform-specific detection native.

```text
React Native Application
          |
          v
DeviceSecurity.getSecurityStatus()
          |
          v
    TurboModule API
       /      \
      /        \
 Android       iOS
  Kotlin      Swift
    |           |
    v           v
Root +       Jailbreak +
Emulator     Simulator
Detection    Detection
    \           /
     \         /
      v       v
   DeviceSecurityStatus
```

## Native source structure

### Android

```text
android/
└── src/main/java/com/shadahmad7/reactnativedevicesecurity/
    ├── EmulatorDetection.kt
    ├── RootDetection.kt
    ├── ReactNativeDeviceSecurityModule.kt
    ├── ReactNativeDeviceSecurityPackage.kt
    └── ReactNativeDeviceSecurityExceptions.kt
```

### iOS

```text
ios/
├── ReactNativeDeviceSecurity.swift
├── ReactNativeDeviceSecurityExceptions.swift
├── RNReactNativeDeviceSecurity.m
└── RNReactNativeDeviceSecuritySpec.h
```

## React Native architecture

The module exposes its native API through a TurboModule specification:

```ts
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

export default TurboModuleRegistry.getEnforcing<Spec>(
  'ReactNativeDeviceSecurity',
);
```

The native implementations are platform-specific while the JavaScript API remains consistent.

## Compatibility

The package is intended for React Native CLI applications and supports Android and iOS through native implementations.

For the exact React Native versions supported by a published package version, refer to its `peerDependencies`.

## FAQ

### Does this require a backend?

No.

All detection is performed locally on the device.

### Does this require Expo?

No.

The package is intended for React Native CLI applications.

### Does `isCompromised` include emulators?

No.

Emulator/simulator status is exposed separately through `isEmulator`.

This separation allows applications to distinguish between:

- A potentially compromised physical device
- A development/test virtual environment

### Can root/jailbreak detection detect every compromised device?

No.

No local detection mechanism can guarantee detection of every compromised environment.

### Can I test root detection on a normal Android Emulator?

Not necessarily.

Emulator detection and root detection are separate checks. A standard emulator can be detected as an emulator without necessarily being detected as rooted.

For meaningful root-detection testing, use a rooted test device or a deliberately configured rooted test environment.

### Can I test jailbreak detection on iOS Simulator?

No.

The simulator can test simulator detection, but it does not represent a jailbroken physical iPhone.

A jailbroken physical device is required for real jailbreak-detection testing.

### Does the library send data anywhere?

No.

The library itself does not send device-security information to a backend.

## License

MIT © Shad Ahmad
