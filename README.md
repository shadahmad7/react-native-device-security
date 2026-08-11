# **@shadahmad7/react-native-device-security**

A lightweight, fully-native **Device Security TurboModule** for React Native CLI applications.

This module provides native device security checks for **Android and iOS**, allowing your React Native application to detect potentially compromised environments such as rooted devices, jailbroken devices, and emulators.

⚡ **TurboModule + Native Kotlin & Swift**

📱 **iOS & Android Support**

🏗️ **New Architecture Ready**

🔒 **Fully Native Device Checks**

🎯 **Single API for React Native**

---

## 🚀 Features

* Fully native React Native TurboModule
* Supports **React Native New Architecture**
* Supports **Android and iOS**
* Detects **rooted Android devices**
* Detects **jailbroken iOS devices**
* Detects **emulators/simulators**
* Provides a unified security status API
* No backend or server-side integration required
* No additional UI maintenance
* Native implementation for platform-specific checks
* Returns a simple security status object to JavaScript

---

## 📦 Installation

```bash
npm install @shadahmad7/react-native-device-security
```

or

```bash
yarn add @shadahmad7/react-native-device-security
```

For iOS, install CocoaPods dependencies:

```bash
cd ios
pod install
```

---

## 💻 Usage

Import the module:

```tsx
import DeviceSecurity from '@shadahmad7/react-native-device-security';
```

You can retrieve the security status using:

```tsx
const securityStatus = await DeviceSecurity.getSecurityStatus();

console.log(securityStatus);
```

Example response:

```ts
{
  isCompromised: false,
  isRooted: false,
  isJailbroken: false,
  isEmulator: false
}
```

---

## 🔐 Security Status

The module exposes four security indicators.

### `isCompromised`

Indicates whether the device is considered potentially compromised.

This value is `true` when one or more security checks identify a potentially compromised environment, such as:

* Rooted Android device
* Jailbroken iOS device
* Emulator/simulator

```tsx
if (securityStatus.isCompromised) {
  // Handle potentially compromised environment
}
```

### `isRooted`

Indicates whether the Android device is detected as rooted.

```tsx
if (securityStatus.isRooted) {
  // Android root detected
}
```

On iOS this value is always:

```ts
false
```

### `isJailbroken`

Indicates whether the iOS device is detected as jailbroken.

```tsx
if (securityStatus.isJailbroken) {
  // iOS jailbreak detected
}
```

On Android this value is always:

```ts
false
```

### `isEmulator`

Indicates whether the application is running in an emulator or simulator environment.

```tsx
if (securityStatus.isEmulator) {
  // Emulator/simulator detected
}
```

---

## 📚 API

### `getSecurityStatus(): Promise<DeviceSecurityStatus>`

Returns the security status of the current device.

```ts
type DeviceSecurityStatus = {
  isCompromised: boolean;
  isRooted: boolean;
  isJailbroken: boolean;
  isEmulator: boolean;
};
```

Example:

```tsx
const status = await DeviceSecurity.getSecurityStatus();

if (status.isCompromised) {
  console.warn('Potentially compromised device');
}
```

---

## 🤖 Android

The Android implementation performs native security checks for:

* Root access
* Root-related indicators
* Emulator detection

The checks are implemented natively using **Kotlin**.

The Android module follows the React Native TurboModule architecture and exposes the security status through the JavaScript API.

### Android result

```ts
{
  isCompromised: boolean,
  isRooted: boolean,
  isJailbroken: false,
  isEmulator: boolean
}
```

---

## 🍎 iOS

The iOS implementation performs native security checks for:

* Jailbreak detection
* Simulator detection

The checks are implemented natively using **Swift**.

### iOS result

```ts
{
  isCompromised: boolean,
  isRooted: false,
  isJailbroken: boolean,
  isEmulator: boolean
}
```

---

## 🏗️ Architecture

The library exposes a single JavaScript API while keeping platform-specific security logic native.

```text
React Native Application
          │
          ▼
DeviceSecurity.getSecurityStatus()
          │
          ▼
   TurboModule Interface
          │
     ┌────┴────┐
     ▼         ▼
 Android      iOS
   Kotlin     Swift
     │         │
     ▼         ▼
Root/Emulator Jailbreak/Simulator
 Detection      Detection
     │         │
     └────┬────┘
          ▼
   DeviceSecurityStatus
```

This keeps security-related logic in one dedicated library while allowing the application to consume a consistent API.

---

## 🧩 React Native Context Integration

The library can be used with a React Context to keep device-security handling centralized in your application.

For example:

```tsx
import DeviceSecurity from '@shadahmad7/react-native-device-security';
import React, { useEffect, useMemo, useState } from 'react';

export const CheckRootedDeviceProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [securityStatus, setSecurityStatus] = useState({
    isCompromised: false,
    isRooted: false,
    isJailbroken: false,
    isEmulator: false,
  });

  useEffect(() => {
    DeviceSecurity.getSecurityStatus().then(setSecurityStatus);
  }, []);

  const value = useMemo(
    () => ({
      securityStatus,
      isCompromised: securityStatus.isCompromised,
    }),
    [securityStatus],
  );

  return (
    <CheckRootedDeviceContext.Provider value={value}>
      {children}
    </CheckRootedDeviceContext.Provider>
  );
};
```

This allows the application to keep all device-security handling in a single place instead of performing native checks throughout the application.

---

## ⚠️ Important Security Considerations

Device compromise detection should be treated as a **security signal**, not as an absolute guarantee.

Rooting and jailbreaking techniques can evolve, and sufficiently modified devices may be able to bypass individual detection mechanisms.

For this reason:

* Do not treat detection as cryptographic proof of device integrity.
* Avoid relying on a single detection technique.
* Use multiple native indicators where appropriate.
* Consider the result as one signal in your application's security model.
* Avoid exposing sensitive security logic unnecessarily to JavaScript.

This library performs local device checks and does **not require backend integration**.

---

## 🧪 Testing

### Android

Root detection should be tested on:

* Standard physical Android devices
* Rooted Android devices
* Android emulators
* Different Android versions

### iOS

Jailbreak detection should be tested on:

* Standard physical iOS devices
* Jailbroken devices where available
* iOS Simulator

Note that simulator/emulator detection is intentionally reported through `isEmulator`.

---

## 🔧 Native Implementation

### Android

Native implementation is written in **Kotlin**.

Main components include:

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

Native implementation is written in **Swift** with the React Native Objective-C bridge/specification.

```text
ios/
├── ReactNativeDeviceSecurity.swift
├── ReactNativeDeviceSecurityExceptions.swift
├── RNReactNativeDeviceSecurity.m
└── RNReactNativeDeviceSecuritySpec.h
```

---

## 📦 Supported React Native Architecture

The library is designed as a React Native TurboModule and supports the **New Architecture**.

The JavaScript specification is defined using:

```ts
import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getSecurityStatus(): Promise<DeviceSecurityStatus>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'ReactNativeDeviceSecurity',
);
```

---

## 🎯 Example

A typical application flow can be:

```tsx
const status = await DeviceSecurity.getSecurityStatus();

if (status.isCompromised) {
  // Show security warning or restrict sensitive functionality.
  return;
}

// Continue normal application flow.
```

---

## ❓ FAQ

### Does this require a backend?

**No.**

All checks are performed locally on the device using native Android and iOS implementations.

### Does it work on both Android and iOS?

Yes.

The JavaScript API is shared while the underlying detection logic is platform-specific.

### Can it detect every rooted or jailbroken device?

No detection mechanism can guarantee that every compromised device will be identified.

The result should be treated as a security signal rather than an absolute guarantee.

### Does it require Expo?

No.

This is designed for **React Native CLI applications**.

### Does it require a third-party backend?

No.

The library performs the checks locally.

### Can I use the result from React Context?

Yes.

The recommended application architecture is to call the native module from a centralized provider/context and expose the resulting security state to the rest of the application.

---

## 📄 License

**MIT © Shad Ahmad**
