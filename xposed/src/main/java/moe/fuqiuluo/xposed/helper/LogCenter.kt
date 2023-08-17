package moe.fuqiuluo.xposed.helper

import moe.fuqiuluo.xposed.actions.impl.toast
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import mqq.app.MobileQQ

internal object LogCenter {
    fun log(
        string: String,
        toast: Boolean = false
    ) {
        if (toast) {
            MobileQQ.getContext().toast(string)
        }
        // 把日志广播到主进程
        DataRequester.request("send_message", bodyBuilder = {
            put("string", string)
        })
    }

}