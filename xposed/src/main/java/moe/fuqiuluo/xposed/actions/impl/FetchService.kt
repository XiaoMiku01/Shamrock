@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import mqq.app.AppRuntime
import mqq.app.MobileQQ
import mqq.app.api.IRuntimeService

class FetchService: IAction {
    override fun invoke(ctx: Context) {
        // 在非主进程载入可能会导致很多问题
        if (MobileQQ.getMobileQQ().qqProcessName != "com.tencent.mobileqq") return

        XposedBridge.hookAllMethods(AppRuntime::class.java, "getRuntimeService", object: XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val service = param.result as? IRuntimeService
                GlobalScope.launch {
                    if (service != null) {
                        NTServiceFetcher.onFetch(service)
                    }
                }
            }
        })
    }
}