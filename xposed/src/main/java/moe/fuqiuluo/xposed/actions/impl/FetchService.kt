@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.ServiceFetcher
import mqq.app.AppRuntime
import mqq.app.api.IRuntimeService

class FetchService: IAction {
    override fun invoke(ctx: Context) {
        XposedBridge.hookAllMethods(AppRuntime::class.java, "getRuntimeService", object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val service = param.result as? IRuntimeService
                GlobalScope.launch {
                    if (service != null) {
                        ServiceFetcher.onFetch(service)
                    }
                }
            }
        })
    }
}