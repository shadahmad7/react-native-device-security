// RNReactNativeDeviceSecuritySpec.h

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@protocol NativeReactNativeDeviceSecuritySpec <NSObject>

- (void)getSecurityStatus:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject;

- (void)isRooted:(RCTPromiseResolveBlock)resolve
        rejecter:(RCTPromiseRejectBlock)reject;

- (void)isJailbroken:(RCTPromiseResolveBlock)resolve
            rejecter:(RCTPromiseRejectBlock)reject;

- (void)isEmulator:(RCTPromiseResolveBlock)resolve
          rejecter:(RCTPromiseRejectBlock)reject;

- (void)isSecurityCompromised:(RCTPromiseResolveBlock)resolve
                     rejecter:(RCTPromiseRejectBlock)reject;

- (void)getRootDetectionResult:(RCTPromiseResolveBlock)resolve
                      rejecter:(RCTPromiseRejectBlock)reject;

@end

#ifdef RCT_NEW_ARCH_ENABLED

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(
  ReactNativeDeviceSecurity,
  ReactNativeDeviceSecurity,
  NSObject
)

RCT_EXTERN_METHOD(
  getSecurityStatus:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
  isRooted:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
  isJailbroken:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
  isEmulator:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
  isSecurityCompromised:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

RCT_EXTERN_METHOD(
  getRootDetectionResult:(RCTPromiseResolveBlock)resolve
  rejecter:(RCTPromiseRejectBlock)reject
)

@end

#endif