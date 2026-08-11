package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider

class DeviceSecurityPackage : TurboReactPackage() {

    override fun getModule(
        name: String,
        reactContext: ReactApplicationContext
    ): NativeModule? {
        return when (name) {
            DeviceSecurityModule.NAME ->
                DeviceSecurityModule(reactContext)

            else -> null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            mapOf(
                DeviceSecurityModule.NAME to com.facebook.react.module.model.ReactModuleInfo(
                    DeviceSecurityModule.NAME,
                    DeviceSecurityModule::class.java.name,
                    false,
                    false,
                    true,
                    false,
                    true
                )
            )
        }
    }
}