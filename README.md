# @shadahmad7/react-native-device-security

A lightweight, native **device security detection TurboModule** for React Native CLI applications.

`@shadahmad7/react-native-device-security` provides a unified JavaScript API for detecting common indicators of potentially compromised or virtualized environments on **Android and iOS**.

All detection is performed locally using native Kotlin and Swift implementations.

The library does **not require a backend or server-side integration**.

> **Security note:** Root, jailbreak, debugger, and runtime-instrumentation detection are security signals, not guarantees of device integrity. A sufficiently modified device may bypass local detection mechanisms.

## Features

### Android

- Native Kotlin implementation
- Root detection
- Root-management application detection
- `su` binary detection
- `su` command detection
- Writable system directory detection
- Dangerous Android system property detection
- Root-related file detection
- Read-write system mount detection
- Android build-tag inspection
- Android emulator detection
- Build fingerprint analysis
- Emulator hardware detection

### iOS

- Native Swift implementation
- Jailbreak detection
- Cydia detection
- Sileo detection
- Zebra detection
- MobileSubstrate detection
- Suspicious filesystem artifact detection
- Sandbox escape detection
- iOS Simulator detection

### Common

- Unified JavaScript API
- Platform-specific native implementations
- TurboModule specification
- React Native New Architecture support
- Legacy architecture compatibility
- No backend integration
- No Expo dependency
- Local-only detection
- Designed for centralized application-level security checks

---

# Comprehensive Security Checks

The library is designed around multiple independent detection signals rather than relying on a single check.

## Android

### Root Detection

The Android implementation currently checks for:

- Root-management applications
- Known `su` binaries
- `su` command availability
- Writable system directories
- Dangerous Android system properties
- Root-related files and directories
- Read-write system mounts
- Dangerous build tags such as `test-keys`

These checks are intentionally independent so that detection does not depend on a single indicator.

### Emulator Detection

Android emulator detection uses native device information including:

- Build fingerprint
- Device model
- Manufacturer
- Brand
- Device
- Product
- Hardware
- `goldfish`
- `ranchu`
- Generic emulator fingerprints
- SDK/emulator product identifiers
- Genymotion indicators

---

## iOS

### Jailbreak Detection

The iOS implementation checks for multiple jailbreak indicators.

#### Package Manager Detection

Detection includes known jailbreak package managers and applications such as:

- Cydia
- Sileo
- Zebra

#### Suspicious Filesystem Artifacts

Detection includes known artifacts associated with:

- Cydia
- Sileo
- MobileSubstrate
- SSH
- APT
- Jailbreak tools

#### Sandbox Escape

The implementation attempts to write to locations outside the application's normal sandbox.

A successful write indicates that the application's sandbox restrictions may have been bypassed.

### Simulator Detection

The iOS implementation uses the native:

```swift
#if targetEnvironment(simulator)
```

environment check.

The simulator is reported through:

```ts
isEmulator: true
```

A simulator is **not** treated as a jailbroken device.

---

# Planned Runtime Security Checks

The following checks can be added as additional defense-in-depth signals.

## Android

### Debugger Detection

Planned checks include:

- Java debugger detection
- `Debug.isDebuggerConnected()`
- `Debug.waitingForDebugger()`
- `TracerPid` inspection
- `ptrace`-based anti-debugging

### Runtime Instrumentation Detection

Planned checks include:

- Runtime thread detection
- `/proc` memory-map inspection
- Suspicious loaded libraries
- Runtime symbol inspection
- Frida-related artifacts
- Runtime instrumentation indicators

## iOS

### Debugger Detection

Planned checks include:

- `sysctl` process inspection
- `P_TRACED` detection
- `ptrace` anti-debugging

### Runtime Instrumentation Detection

Planned checks include:

- Injected dylib detection
- Loaded image inspection
- Suspicious runtime threads
- Runtime symbol inspection
- Frida-related indicators
- Dynamic instrumentation indicators

> These checks should only be documented as implemented after they are added to the native implementations.

---

# Installation

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

---

# Usage

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

---

# API

## `getSecurityStatus()`

```ts
getSecurityStatus(): Promise<DeviceSecurityStatus>
```

Returns the complete device-security status.

```ts
type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;
};
```

## `isSecurityCompromised()`

```ts
isSecurityCompromised(): Promise<boolean>
```

Returns the library's current compromise signal.

For the current implementation:

### Android

```text
isCompromised = isRooted
```

### iOS

```text
isCompromised = isJailbroken
```

Use the individual properties if your application needs separate policies for compromised physical devices and virtual environments.

## `isRooted()`

```ts
isRooted(): Promise<boolean>
```

Returns whether Android root indicators were detected.

On iOS this returns:

```ts
false
```

## `isJailbroken()`

```ts
isJailbroken(): Promise<boolean>
```

Returns whether iOS jailbreak indicators were detected.

On Android this returns:

```ts
false
```

## `isEmulator()`

```ts
isEmulator(): Promise<boolean>
```

Returns whether the application is running in an emulator/simulator environment.

Android:

```text
Android Emulator → true
Physical Android → false
```

iOS:

```text
iOS Simulator → true
Physical iPhone → false
```

## `getRootDetectionResult()`

```ts
getRootDetectionResult(): Promise<RootDetectionResult>
```

Returns detailed Android root-detection results.

```ts
type RootDetectionChecks = {
  rootManagementApp: boolean;
  dangerousBuildTags: boolean;
  suBinary: boolean;
  suCommand: boolean;
  writableSystemDirectories: boolean;
  dangerousProperties: boolean;
  rootFiles: boolean;
  rwSystemMounts: boolean;
};

type RootDetectionResult = {
  isRooted: boolean;
  checks: RootDetectionChecks;
};
```

Example:

```ts
{
  isRooted: true,
  checks: {
    rootManagementApp: true,
    dangerousBuildTags: false,
    suBinary: true,
    suCommand: true,
    writableSystemDirectories: false,
    dangerousProperties: false,
    rootFiles: true,
    rwSystemMounts: false
  }
}
```

---

# Platform Behavior

## Android

```ts
{
  isCompromised: boolean,
  isRooted: boolean,
  isJailbroken: false,
  isEmulator: boolean
}
```

The Android implementation performs native checks for:

```text
Root
 ├── Root management apps
 ├── su binaries
 ├── su command
 ├── Writable system directories
 ├── Dangerous properties
 ├── Root files
 ├── RW system mounts
 └── Build tags

Emulator
 ├── Build fingerprint
 ├── Model
 ├── Manufacturer
 ├── Brand
 ├── Device
 ├── Product
 └── Hardware
```

## iOS

```ts
{
  isCompromised: boolean,
  isRooted: false,
  isJailbroken: boolean,
  isEmulator: boolean
}
```

The iOS implementation performs native checks for:

```text
Jailbreak
 ├── Cydia
 ├── Sileo
 ├── Zebra
 ├── MobileSubstrate
 ├── Suspicious filesystem artifacts
 └── Sandbox escape

Simulator
 └── targetEnvironment(simulator)
```

---

# Recommended Application Architecture

For applications that need device-security information throughout the application, call the native module once from a centralized React Context/provider.

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
      .catch(error => {
        console.error(
          'Device security detection failed:',
          error,
        );
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

Centralizing the detection avoids repeatedly invoking native security checks throughout the application.

---

# Security Considerations

This library provides **local device-security signals**.

It is not intended to provide absolute device integrity guarantees.

Detection can potentially be bypassed using:

- Modified operating systems
- Runtime hooking
- Dynamic instrumentation
- Root/jailbreak concealment
- Reverse engineering
- Application patching
- Native code modification
- Runtime API interception

Therefore:

- Do not treat `isCompromised === false` as proof that a device is secure.
- Do not rely on a single detection mechanism for high-risk operations.
- Combine multiple independent signals when appropriate.
- Keep sensitive security decisions out of JavaScript where practical.
- Consider server-side/platform attestation for high-value operations.
- Apply application-specific security policies to the returned status.

The library itself does not transmit device-security information to a backend.

---

# OWASP Alignment

The detection approach is informed by security-resilience concepts described in the OWASP Mobile Application Security Testing Guide (MASTG).

Relevant areas include:

- Root detection
- Jailbreak detection
- Emulator detection
- Debugger detection
- Runtime instrumentation detection
- Application resilience

See:

https://mas.owasp.org/MASTG/

> OWASP guidance is a security-testing and resilience reference. Local detection mechanisms are inherently bypassable on a sufficiently compromised device.

---

# Testing

## Android

Test on:

- Normal physical Android device
- Android Emulator
- Rooted Android test device
- Rooted/configured emulator

Normal device:

```ts
{
  isRooted: false,
  isEmulator: false
}
```

Android Emulator:

```ts
{
  isEmulator: true
}
```

Rooted device:

```ts
{
  isRooted: true
}
```

Use `getRootDetectionResult()` to determine which individual root checks triggered.

## iOS

Test on:

- Normal physical iPhone
- iOS Simulator
- Jailbroken test device

iOS Simulator:

```ts
{
  isEmulator: true,
  isJailbroken: false
}
```

Normal physical iPhone:

```ts
{
  isEmulator: false,
  isJailbroken: false
}
```

Jailbreak detection requires a jailbroken physical device for meaningful validation.

---

# Architecture

```text
React Native Application
          |
          v
DeviceSecurity.getSecurityStatus()
          |
          v
     TurboModule API
          |
     +----+----+
     |         |
     v         v
 Android     iOS
  Kotlin     Swift
     |         |
     v         v
   Root     Jailbreak
   +          +
 Emulator   Simulator
 Detection  Detection
     |         |
     +----+----+
          |
          v
 DeviceSecurityStatus
```

The JavaScript API remains consistent while the security implementation is platform-specific.

---

# Native Source Structure

## Android

```text
android/
└── src/main/java/com/shadahmad7/reactnativedevicesecurity/
    ├── EmulatorDetection.kt
    ├── RootDetection.kt
    ├── ReactNativeDeviceSecurityModule.kt
    ├── ReactNativeDeviceSecurityPackage.kt
    └── ReactNativeDeviceSecurityExceptions.kt
```

## iOS

```text
ios/
├── EmulatorDetection.swift
├── JailbreakDetection.swift
├── ReactNativeDeviceSecurity.swift
├── ReactNativeDeviceSecurityExceptions.swift
├── RNReactNativeDeviceSecurity.m
└── RNReactNativeDeviceSecuritySpec.h
```

---

# React Native Architecture

The library exposes its native API through a TurboModule specification.

```ts
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

export default TurboModuleRegistry.getEnforcing<Spec>(
  'ReactNativeDeviceSecurity',
);
```

The native implementations are platform-specific while the JavaScript API remains consistent.

---

# React Native Architecture Compatibility

The package is intended for React Native CLI applications and supports:

- Android
- iOS
- React Native New Architecture
- Legacy architecture compatibility

For exact React Native versions supported by a published package version, refer to its `peerDependencies`.

---

# FAQ

### Does this require a backend?

No.

All current detection is performed locally on the device.

### Does this require Expo?

No.

The package is intended for React Native CLI applications.

### Does `isCompromised` include emulators?

No, in the current implementation.

Android:

```text
isCompromised = isRooted
```

iOS:

```text
isCompromised = isJailbroken
```

Use the individual properties if your application needs different policies.

### Does the library detect Frida?

Comprehensive Frida and runtime-instrumentation detection is planned as a dedicated security layer.

The current implementation should **not** be considered comprehensive Frida detection.

### Can root/jailbreak detection detect every compromised device?

No.

No local detection mechanism can guarantee detection of every compromised environment.

### Can I test root detection on a normal Android Emulator?

Not necessarily.

Emulator detection and root detection are separate signals.

A standard emulator can return:

```ts
{
  isEmulator: true,
  isRooted: false
}
```

For meaningful root-detection testing, use a rooted test device or deliberately configured rooted environment.

### Can I test jailbreak detection on iOS Simulator?

No.

The simulator can test simulator detection but does not represent a jailbroken physical iPhone.

A jailbroken physical device is required for meaningful jailbreak-detection testing.

### Does the library send data anywhere?

No.

The library performs its detection locally and does not transmit device-security information to a backend.

---

# Roadmap

## Android

- [ ] Debugger detection
- [ ] `TracerPid` inspection
- [ ] `ptrace` anti-debugging
- [ ] Runtime thread detection
- [ ] `/proc` memory-map scanning
- [ ] Runtime symbol detection
- [ ] Loaded library inspection
- [ ] Frida detection
- [ ] Runtime instrumentation detection

## iOS

- [ ] Debugger detection
- [ ] `sysctl` inspection
- [ ] `ptrace` anti-debugging
- [ ] Injected dylib detection
- [ ] Suspicious thread detection
- [ ] Runtime symbol detection
- [ ] Frida detection
- [ ] Runtime instrumentation detection

## Platform Integrity

- [ ] Android Play Integrity integration
- [ ] Apple App Attest integration
- [ ] DeviceCheck integration
- [ ] Server-side security policy evaluation

---

# License

MIT © Shad Ahmad
