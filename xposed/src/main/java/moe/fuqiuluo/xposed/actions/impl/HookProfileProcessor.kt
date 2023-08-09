package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import com.tencent.mobileqq.profilecard.processor.ProfileBusinessProcessorFactory
import com.tencent.mobileqq.service.ProfileProcessor
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.actions.IAction
import mqq.app.AppRuntime
import kotlin.reflect.jvm.isAccessible

internal class HookProfileProcessor: IAction {
    override fun invoke(ctx: Context) {
        /*
        kotlin.runCatching {
            val filed = ProfileBusinessProcessorFactory::sInjectProcessorClasses
            val method = ProfileBusinessProcessorFactory::class.java
                .getDeclaredMethod("initBusinessProcessors", AppRuntime::class.java)
            XposedBridge.hookMethod(method, object: XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!filed.isAccessible) {
                        filed.isAccessible = true
                    }
                    val processors = filed.get()
                    processors.add(ProfileProcessor::class.java)
                }
            })
        }.onFailure {
            XposedBridge.log("无法注入ProfileCard监听器。")
        }*/
    }
}