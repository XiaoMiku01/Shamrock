@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.xposed.actions.IAction
import mqq.app.MobileQQ

class CreateHTTP: IAction {
    override fun invoke(ctx: Context) {
        if (MobileQQ.getMobileQQ().qqProcessName != "com.tencent.mobileqq") return

        val shamrockConfig = ctx.getSharedPreferences("shamrock_config", 0)
        val port = shamrockConfig.getInt("port", 5700)

        GlobalScope.launch {
            kotlin.runCatching {
                HTTPServer.start(port)
            }.onFailure {
                XposedBridge.log(it)
            }
        }
    }
}