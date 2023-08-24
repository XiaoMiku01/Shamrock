package moe.fuqiuluo.xposed.actions

import android.content.Context
import com.tencent.common.config.pad.DeviceType
import com.tencent.mobileqq.helper.ShamrockConfig
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.helper.LogCenter
import com.tencent.qqnt.utils.PlatformUtils
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.fuqiuluo.xposed.tools.FuzzySearchClass

internal class ForceTablet: IAction {
    override fun invoke(ctx: Context) {
        if (ShamrockConfig.forceTablet()) {
            if (PlatformUtils.isMainProcess()) {
                LogCenter.log("强制协议类型 (PAD)", toast = true)
            }
            FuzzySearchClass.findAllClassByMethod(
                LuoClassloader.hostClassLoader, "com.tencent.common.config.pad"
            ) { _, method ->
                method.returnType == DeviceType::class.java
            }.forEach { clazz ->
                //log("Inject to tablet mode in ${clazz.name}")
                val method = clazz.declaredMethods.first { it.returnType == DeviceType::class.java }
                XposedBridge.hookMethod(method, object: XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        //log("Original deviceMode = ${param.result}, but change to TABLET.")
                        param.result = DeviceType.TABLET
                    }
                })
            }
        }
    }
}