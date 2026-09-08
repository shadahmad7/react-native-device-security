# @shadahmad7/react-native-device-security

A lightweight, native **device security detection TurboModule for React Native CLI applications**, providing **OWASP-focused root and jailbreak detection** for Android and iOS.

`@shadahmad7/react-native-device-security` provides a unified JavaScript API for detecting common indicators of potentially compromised or virtualized environments, including:

* Android root detection
* iOS jailbreak detection
* Android emulator detection
* iOS simulator detection

All detection is performed locally using native Kotlin and Swift implementations.

The library does **not require a backend or server-side integration**.

> **Security note:** Root, jailbreak, emulator, and simulator detection are security signals, not guarantees of device integrity. A sufficiently modified device may bypass local detection mechanisms.

---

# Features

## Android

* Native Kotlin implementation
* OWASP-focused root detection
* Root-management application detection
* `su` binary detection
* `su` root-access detection
* Writable system directory inspection
* Dangerous Android system property inspection
* Root-related file and directory inspection
* Read-write system mount inspection
* Android build-tag inspection
* Android emulator detection
* Build fingerprint analysis
* Emulator hardware detection

## iOS

* Native Swift implementation
* Jailbreak detection
* Cydia detection
* Sileo detection
* Zebra detection
* MobileSubstrate detection
* Suspicious filesystem artifact detection
* Sandbox escape detection
* iOS Simulator detection

## Common

* Unified JavaScript API
* Platform-specific native implementations
* TurboModule specification
* React Native New Architecture support
* Legacy architecture compatibility
* OWASP MASTG-focused security checks
* Local-only detection
* No backend integration
* No Expo dependency
* Designed for centralized application-level security checks

---

# Detection Model

The library separates **root detection signals** from **diagnostic checks**.

This distinction is important because not every security indicator is sufficient by itself to classify a device as rooted.

## Strong Root Signals

The following signals can contribute directly to `isRooted`:

* Root-management application detected
* Known `su` binary detected
* Successful `su` root-access check

When one or more strong signals are detected:

```text
isRooted = true
```

The corresponding signals are returned in:

```ts
reasons
```

## Diagnostic Signals

The library also performs additional checks that can provide useful security information but are **not independently treated as proof of root**.

These include:

* Dangerous build tags
* Dangerous system properties
* Root-related files
* Read-write system mounts
* Writable system directories

These values are returned through:

```ts
checks
```

They do not automatically make:

```ts
isRooted
```

`true`.

This design helps reduce false positives on modern Android devices where filesystem layout, mount namespaces, dynamic partitions, and system configuration can produce indicators that resemble traditional rooted environments.

For example, a device may return:

```ts
{
  isRooted: false,
  reasons: [],
  checks: {
    rootFiles: true,
    rwSystemMounts: true
  }
}
```

This means diagnostic indicators were detected, but no sufficiently strong root signal was confirmed.

---

# OWASP Security Approach

The library is designed around **multiple independent device-security signals** rather than relying on a single root or jailbreak check.

The detection strategy is informed by the **OWASP Mobile Application Security Testing Guide (MASTG)** and mobile application resilience principles.

The objective is to make compromise detection more resilient by combining multiple indicators while avoiding classification based solely on weak signals.

For Android, root detection includes:

### Root confirmation signals

* Root-management applications
* Known `su` binaries
* `su` root-access detection

### Supporting diagnostic signals

* Writable system directories
* Dangerous Android system properties
* Root-related files and directories
* Read-write system mounts
* Dangerous build tags such as `test-keys`

The distinction between confirmation and diagnostic signals is intentional.

> OWASP provides security guidance and testing methodologies. This library should not be interpreted as providing an absolute guarantee of device integrity or protection against all forms of compromise.

See the OWASP Mobile Application Security Testing Guide:

https://mas.owasp.org/MASTG/

---

# Comprehensive Security Checks

The library uses multiple independent detection signals to reduce reliance on a single indicator.

## Android

### Root Detection

The Android implementation performs the following checks:

| Check                       | Purpose                                     | Contributes directly to `isRooted` |
| --------------------------- | ------------------------------------------- | ---------------------------------- |
| Root-management app         | Detect known root-management applications   | Yes                                |
| `su` binary                 | Detect known `su` binaries                  | Yes                                |
| `su` command                | Verify available root access                | Yes                                |
| Writable system directories | Inspect system filesystem permissions       | No                                 |
| Dangerous properties        | Inspect Android security-related properties | No                                 |
| Root files                  | Detect known root-related artifacts         | No                                 |
| RW system mounts            | Inspect system mount configuration          | No                                 |
| Build tags                  | Detect tags such as `test-keys`             | No                                 |

This separation is intentional.

A diagnostic check may indicate an unusual or security-relevant configuration without proving that the application currently has root privileges.

### Root Reasons

When root is detected, the `reasons` array contains the strong signals that caused the root classification.

Example:

```ts
{
  isRooted: true,
  reasons: [
    "ROOT_MANAGEMENT_APP_DETECTED",
    "SU_COMMAND_DETECTED"
  ]
}
```

If only diagnostic checks are triggered:

```ts
{
  isRooted: false,
  reasons: []
}
```

while the individual checks remain available through `checks`.

---

## Emulator Detection

Android emulator detection uses native device information including:

* Build fingerprint
* Device model
* Manufacturer
* Brand
* Device
* Product
* Hardware
* `goldfish`
* `ranchu`
* Generic emulator fingerprints
* SDK/emulator product identifiers
* Genymotion indicators

Emulator detection is independent from root detection.

Therefore:

```text
Android Emulator
    isEmulator = true
    isRooted = false
```

is a valid result.

---

# iOS

## Jailbreak Detection

The iOS implementation checks for multiple jailbreak indicators.

### Package Manager Detection

Detection includes known jailbreak package managers and applications such as:

* Cydia
* Sileo
* Zebra

### Suspicious Filesystem Artifacts

Detection includes known artifacts associated with:

* Cydia
* Sileo
* MobileSubstrate
* SSH
* APT
* Jailbreak tools

### Sandbox Escape

The implementation attempts to write to locations outside the application's normal sandbox.

A successful write indicates that the application's sandbox restrictions may have been bypassed.

### Simulator Detection

The implementation uses the native:

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

# Detection Reasons

Security results expose **reasons** explaining why a security signal was detected.

## Root Reasons

Android root reasons represent strong root-detection signals.

Possible values include:

```text
ROOT_MANAGEMENT_APP_DETECTED
SU_BINARY_DETECTED
SU_COMMAND_DETECTED
```

These reasons contribute to:

```ts
isRooted: true
```

## Emulator Reasons

When emulator detection is triggered, the emulator-specific reasons are returned separately.

Example:

```ts
{
  detected: true,
  reasons: [
    "EMULATOR_DETECTED"
  ]
}
```

## Jailbreak Reasons

On iOS, jailbreak detection returns the jailbreak indicators that were detected.

Examples can include identifiers associated with:

```text
CYDIA_INSTALLED
SILEO_INSTALLED
ZEBRA_INSTALLED
MOBILE_SUBSTRATE
APT_ARTIFACT
SANDBOX_ESCAPE
```

## Compromise Reasons

`compromiseReasons` combines the applicable platform security reasons.

For example:

```ts
{
  isCompromised: true,
  compromiseReasons: [
    "SU_COMMAND_DETECTED"
  ]
}
```

On an emulator:

```ts
{
  isCompromised: true,
  compromiseReasons: [
    "EMULATOR_DETECTED"
  ]
}
```

> Applications should use the boolean security properties for policy decisions rather than assuming that every diagnostic check represents confirmed compromise.

---

# Planned Runtime Security Checks

The following checks are planned as additional defense-in-depth security signals.

These checks should **not** be considered implemented until they are added to the native implementations.

## Android

### Debugger Detection

Planned checks include:

* Java debugger detection
* `Debug.isDebuggerConnected()`
* `Debug.waitingForDebugger()`
* `TracerPid` inspection
* `ptrace`-based anti-debugging

### Runtime Instrumentation Detection

Planned checks include:

* Runtime thread detection
* `/proc` memory-map inspection
* Suspicious loaded libraries
* Runtime symbol inspection
* Frida-related artifacts
* Runtime instrumentation indicators

## iOS

### Debugger Detection

Planned checks include:

* `sysctl` process inspection
* `P_TRACED` detection
* `ptrace` anti-debugging

### Runtime Instrumentation Detection

Planned checks include:

* Injected dylib detection
* Loaded image inspection
* Suspicious runtime threads
* Runtime symbol inspection
* Frida-related indicators
* Dynamic instrumentation indicators

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
  isEmulator: false,
  rootReasons: [],
  jailbreakReasons: [],
  emulatorReasons: [],
  compromiseReasons: []
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

  rootReasons?: string[];
  jailbreakReasons?: string[];
  emulatorReasons?: string[];
  compromiseReasons?: string[];
};
```

### Android

Example:

```ts
{
  isCompromised: true,
  isRooted: true,
  isJailbroken: false,
  isEmulator: false,

  rootReasons: [
    "SU_COMMAND_DETECTED"
  ],

  jailbreakReasons: [],

  emulatorReasons: [],

  compromiseReasons: [
    "SU_COMMAND_DETECTED"
  ]
}
```

### Diagnostic checks

Detailed Android root diagnostics are available separately through:

```ts
getRootDetectionResult()
```

This keeps diagnostic information separate from the primary root decision.

---

## `isSecurityCompromised()`

```ts
isSecurityCompromised(): Promise<boolean>
```

Returns the library's current compromise signal.

### Android

```text
isCompromised = isRooted || isEmulator
```

### iOS

```text
isCompromised = isJailbroken || isEmulator
```

Use the individual properties if your application needs separate policies for:

* rooted devices
* jailbroken devices
* emulators
* simulators

> If your application wants to block only compromised physical devices, use `isRooted` / `isJailbroken` instead of treating emulator detection as equivalent to root or jailbreak.

---

## `isRooted()`

```ts
isRooted(): Promise<boolean>
```

Returns whether Android root indicators were detected.

On iOS this returns:

```ts
false
```

The result is based on strong root-detection signals rather than diagnostic indicators alone.

---

## `isJailbroken()`

```ts
isJailbroken(): Promise<boolean>
```

Returns whether iOS jailbreak indicators were detected.

On Android this returns:

```ts
false
```

---

## `isEmulator()`

```ts
isEmulator(): Promise<boolean>
```

Returns whether the application is running in an emulator or simulator environment.

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

---

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
  reasons: string[];
  checks: RootDetectionChecks;
};
```

### Example: confirmed root

```ts
{
  isRooted: true,

  reasons: [
    "SU_COMMAND_DETECTED"
  ],

  checks: {
    rootManagementApp: false,
    dangerousBuildTags: false,
    suBinary: true,
    suCommand: true,
    writableSystemDirectories: false,
    dangerousProperties: false,
    rootFiles: false,
    rwSystemMounts: false
  }
}
```

### Example: diagnostic indicators only

A normal modern Android device may report diagnostic indicators without being classified as rooted:

```ts
{
  isRooted: false,

  reasons: [],

  checks: {
    rootManagementApp: false,
    dangerousBuildTags: false,
    suBinary: false,
    suCommand: false,
    writableSystemDirectories: false,
    dangerousProperties: false,
    rootFiles: true,
    rwSystemMounts: true
  }
}
```

In this case:

```ts
isRooted === false
```

because no strong root signal was confirmed.

This distinction helps prevent false positives caused by modern Android filesystem and mount configurations.

---

# Platform Behavior

## Android

```ts
{
  isCompromised: boolean,
  isRooted: boolean,
  isJailbroken: false,
  isEmulator: boolean,

  rootReasons: string[],
  jailbreakReasons: string[],
  emulatorReasons: string[],
  compromiseReasons: string[]
}
```

The Android implementation performs native checks for:

```text
Root
 ├── Root management apps        ← root signal
 ├── su binaries                 ← root signal
 ├── su root access              ← root signal
 ├── Writable system directories ← diagnostic
 ├── Dangerous properties        ← diagnostic
 ├── Root files                 ← diagnostic
 ├── RW system mounts           ← diagnostic
 └── Build tags                 ← diagnostic

Emulator
 ├── Build fingerprint
 ├── Model
 ├── Manufacturer
 ├── Brand
 ├── Device
 ├── Product
 └── Hardware
```

The distinction between root signals and diagnostics is intentional.

---

## iOS

```ts
{
  isCompromised: boolean,
  isRooted: false,
  isJailbroken: boolean,
  isEmulator: boolean,

  jailbreakReasons: string[],
  compromiseReasons: string[]
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

For applications that need device-security information throughout the application, call the native module once from a centralized React Context or provider.

```tsx
import DeviceSecurity, {
  type DeviceSecurityStatus,
} from '@shadahmad7/react-native-device-security';

import React, {
  useEffect,
  useMemo,
  useState,
} from 'react';

const initialStatus: DeviceSecurityStatus = {
  isCompromised: false,
  isRooted: false,
  isJailbroken: false,
  isEmulator: false,
  rootReasons: [],
  jailbreakReasons: [],
  emulatorReasons: [],
  compromiseReasons: [],
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

Centralizing detection avoids repeatedly invoking native security checks throughout the application.

---

# Security Considerations

This library provides **local device-security signals** based on multiple native detection mechanisms.

It is not intended to provide absolute device-integrity guarantees.

Detection can potentially be bypassed using:

* Modified operating systems
* Runtime hooking
* Dynamic instrumentation
* Root/jailbreak concealment
* Reverse engineering
* Application patching
* Native code modification
* Runtime API interception

Therefore:

* Do not treat `isCompromised === false` as proof that a device is secure.
* Do not rely on a single detection mechanism for high-risk operations.
* Use `reasons` to understand which strong signals caused a detection.
* Use `checks` for diagnostic and security-analysis information.
* Do not automatically classify every diagnostic check as confirmed root.
* Keep sensitive security decisions out of JavaScript where practical.
* Consider platform attestation for high-value operations.
* Apply application-specific security policies to the returned status.

The library itself does not transmit device-security information to a backend.

---

# OWASP Alignment

The library's root and device-security detection approach is designed around security-resilience concepts described by the **OWASP Mobile Application Security Testing Guide (MASTG)**.

Relevant areas include:

* Root detection
* Jailbreak detection
* Emulator detection
* Debugger detection
* Runtime instrumentation detection
* Application resilience

The current Android implementation uses multiple root-detection signals rather than depending on a single check.

These signals are separated into:

```text
Strong Root Signals
        │
        ├── Root management application
        ├── su binary
        └── Successful su root access
        │
        ▼
     isRooted
```

and:

```text
Diagnostic Signals
        │
        ├── Build tags
        ├── System properties
        ├── Root files
        ├── RW mounts
        └── Writable directories
        │
        ▼
      checks
```

This distinction reduces the likelihood that a normal modern Android configuration is incorrectly classified as rooted.

OWASP reference:

https://mas.owasp.org/MASTG/

> **Important:** OWASP alignment does not mean that local detection can guarantee device integrity. A sufficiently compromised device may bypass local security checks.

---

# Testing

## Android

Test on:

* Normal physical Android device
* Android Emulator
* Rooted Android test device
* Rooted/configured emulator

### Normal device

```ts
{
  isRooted: false,
  isEmulator: false
}
```

A normal physical device may still have diagnostic checks enabled:

```ts
{
  isRooted: false,

  checks: {
    rootFiles: true,
    rwSystemMounts: true
  }
}
```

This does not necessarily indicate root.

### Android Emulator

```ts
{
  isEmulator: true,
  isRooted: false
}
```

### Rooted device

```ts
{
  isRooted: true,
  reasons: [
    "SU_COMMAND_DETECTED"
  ]
}
```

Use:

```ts
getRootDetectionResult()
```

to determine:

1. Whether the device was classified as rooted.
2. Which strong root reasons caused the classification.
3. Which additional diagnostic checks were triggered.

---

## iOS

Test on:

* Normal physical iPhone
* iOS Simulator
* Jailbroken test device

### iOS Simulator

```ts
{
  isEmulator: true,
  isJailbroken: false
}
```

### Normal physical iPhone

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

          │
          ▼

DeviceSecurity.getSecurityStatus()

          │
          ▼

     TurboModule API

          │
     ┌────┴────┐
     │         │
     ▼         ▼
 Android     iOS
  Kotlin     Swift
     │         │
     ▼         ▼
   Root     Jailbreak
     +          +
 Emulator   Simulator
 Detection  Detection
     │         │
     └────┬────┘
          │
          ▼

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
import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

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

---

# React Native Architecture Compatibility

The package is intended for React Native CLI applications and supports:

* Android
* iOS
* React Native New Architecture
* Legacy architecture compatibility
* TurboModule-based native integration

For exact React Native versions supported by a published package version, refer to its `peerDependencies`.

---

# FAQ

## Does this provide OWASP root detection?

The Android root-detection implementation follows an **OWASP MASTG-focused, multi-signal approach** using several independent native checks.

The library distinguishes between:

* strong root signals used to determine `isRooted`
* diagnostic signals exposed through `checks`

It should be used as part of a broader mobile application security strategy rather than as the sole security control.

## Does this require a backend?

No.

All current detection is performed locally on the device.

## Does this require Expo?

No.

The package is intended for React Native CLI applications.

## What are detection reasons?

Detection reasons explain which security signals caused a positive detection.

For Android root detection, `reasons` contains strong root signals such as:

```text
ROOT_MANAGEMENT_APP_DETECTED
SU_BINARY_DETECTED
SU_COMMAND_DETECTED
```

Additional checks such as root files and read-write mounts are available through `checks`.

## Are all checks treated as root?

No.

This is an important part of the detection model.

Checks such as:

```text
ROOT_FILE_DETECTED
RW_SYSTEM_MOUNT_DETECTED
DANGEROUS_BUILD_TAGS_DETECTED
```

are diagnostic indicators.

They do not independently make:

```ts
isRooted === true
```

This helps avoid false positives on modern Android devices.

## Does `isCompromised` include emulators?

Yes, in the current implementation.

Android:

```text
isCompromised = isRooted || isEmulator
```

iOS:

```text
isCompromised = isJailbroken || isEmulator
```

Use the individual properties if your application needs different policies.

## Does the library detect Frida?

Comprehensive Frida and runtime-instrumentation detection is planned as a dedicated security layer.

The current implementation should **not** be considered comprehensive Frida detection.

## Can root/jailbreak detection detect every compromised device?

No.

No local detection mechanism can guarantee detection of every compromised environment.

## Can I test root detection on a normal Android Emulator?

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

## Can I test jailbreak detection on iOS Simulator?

No.

The simulator can test simulator detection but does not represent a jailbroken physical iPhone.

A jailbroken physical device is required for meaningful jailbreak-detection testing.

## Does the library send data anywhere?

No.

The library performs its detection locally and does not transmit device-security information to a backend.

---

# Roadmap

## Android

* [ ] Debugger detection
* [ ] `TracerPid` inspection
* [ ] `ptrace` anti-debugging
* [ ] Runtime thread detection
* [ ] `/proc` memory-map scanning
* [ ] Runtime symbol detection
* [ ] Loaded library inspection
* [ ] Frida detection
* [ ] Runtime instrumentation detection

## iOS

* [ ] Debugger detection
* [ ] `sysctl` inspection
* [ ] `ptrace` anti-debugging
* [ ] Injected dylib detection
* [ ] Suspicious thread detection
* [ ] Runtime symbol detection
* [ ] Frida detection
* [ ] Runtime instrumentation detection

## Platform Integrity

* [ ] Android Play Integrity integration
* [ ] Apple App Attest integration
* [ ] DeviceCheck integration
* [ ] Server-side security policy evaluation

---

# License

MIT © Shad Ahmad
