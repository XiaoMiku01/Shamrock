@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions

import android.content.Context
import com.tencent.mobileqq.helper.ShamrockConfig
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.remote.HTTPServer
import com.tencent.qqnt.utils.PlatformUtils

internal class CreateHTTP: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMainProcess()) return

        val port = ShamrockConfig.getPort()

        GlobalScope.launch {
            try {
                HTTPServer.start(port)
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }
    }
}