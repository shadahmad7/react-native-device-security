#import <React/RCTBridgeModule.h>
#import <React/RCTTurboModule.h>

@interface RCT_EXTERN_MODULE(ReactNativeDeviceSecurity, NSObject)

RCT_EXTERN_METHOD(
  getSecurityStatus:
  (RCTPromiseResolveBlock)resolve
  rejecter:
  (RCTPromiseRejectBlock)reject
)

@end