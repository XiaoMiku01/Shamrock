package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import moe.fuqiuluo.xposed.actions.IAction
import mqq.app.MobileQQ

class CreateHTTP: IAction {
    override fun invoke(ctx: Context) {
        if (MobileQQ.getMobileQQ().qqProcessName != "com.tencent.mobileqq") return

        val shamrockConfig = ctx.getSharedPreferences("shamrock_config", 0)
        val port = shamrockConfig.getInt("port", 5700)

    }
}