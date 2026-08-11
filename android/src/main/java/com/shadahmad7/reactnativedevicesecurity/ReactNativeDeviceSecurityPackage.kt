// ReactNativeDeviceSecurityPackage.kt

package com.shadahmad7.reactnativedevicesecurity

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

class ReactNativeDeviceSecurityPackage : BaseReactPackage() {

    override fun getModule(
        name: String,
        reactContext: ReactApplicationContext
    ): NativeModule? {
        return if (name == ReactNativeDeviceSecurityModule.NAME) {
            ReactNativeDeviceSecurityModule(reactContext)
        } else {
            null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            mapOf(
                ReactNativeDeviceSecurityModule.NAME to ReactModuleInfo(
                    ReactNativeDeviceSecurityModule.NAME,
                    ReactNativeDeviceSecurityModule.NAME,
                    false,
                    false,
                    false,
                    true
                )
            )
        }
    }

    override fun createViewManagers(
        reactContext: ReactApplicationContext
    ): List<ViewManager<*, *>> {
        return emptyList()
    }
}