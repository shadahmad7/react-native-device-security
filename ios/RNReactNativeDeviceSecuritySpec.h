// RNReactNativeDeviceSecuritySpec.h

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@protocol NativeReactNativeDeviceSecuritySpec <NSObject>

- (void)getSecurityStatus:(RCTPromiseResolveBlock)resolve
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

@end
#endif