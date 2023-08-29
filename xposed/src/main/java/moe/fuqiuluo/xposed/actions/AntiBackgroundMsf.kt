package moe.fuqiuluo.xposed.actions

import android.content.Context
import de.robv.android.xposed.XposedHelpers
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.fuqiuluo.xposed.tools.hookMethod
import mqq.app.MobileQQ

internal class AntiBackgroundMsf: IAction {
    override fun invoke(ctx: Context) {
        kotlin.runCatching {
            XposedHelpers.findClass("com.tencent.mobileqq.activity.miniaio.MiniMsgUser", LuoClassloader)
        }.onSuccess {
            it.hookMethod("onBackground").before {
                it.result = null
            }
        }

        try {
            val application = MobileQQ.getMobileQQ()
            application.javaClass.hookMethod("onActivityFocusChanged").before {
                it.args[1] = true
            }
        } catch (e: Throwable) {
            LogCenter.log("Keeping MSF alive failed: ${e.message}")
        }


    }
}