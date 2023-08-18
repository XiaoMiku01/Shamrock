package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import com.tencent.common.config.pad.DeviceType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.fuqiuluo.xposed.tools.FuzzySearchClass

class ForceTablet: IAction {
    override fun invoke(ctx: Context) {
        val preferences = ctx.getSharedPreferences("shamrock_config", 0)
        if (preferences.getBoolean("tablet", true)) {
            if (PlatformHelper.isMainProcess()) {
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