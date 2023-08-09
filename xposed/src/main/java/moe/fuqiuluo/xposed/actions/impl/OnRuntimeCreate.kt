package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.actions.IAction
import mqq.app.AppRuntime
import mqq.app.MobileQQ
import kotlin.reflect.jvm.javaMethod

internal class OnRuntimeCreate: IAction {
    private val runningLock = mutableSetOf<String>()

    override fun invoke(ctx: Context) {
        val member = AppRuntime::onCreate.javaMethod
        XposedBridge.hookMethod(member, object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val procName = MobileQQ.getMobileQQ().qqProcessName
                if (runningLock.contains(procName)) return
                val runtime = param.thisObject as AppRuntime



                runningLock.add(procName)
            }
        })
    }
}