package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import com.tencent.mobileqq.data.BotStatus
import com.tencent.mobileqq.data.Self
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import mqq.app.MobileQQ

internal object GetStatus: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val curUin = runtime.currentAccountUin
        return resultToString(true, Status.Ok, listOf(
            BotStatus(
                Self("qq", curUin), runtime.isLogin, "正常"
            )
        ))
    }

    override fun path(): String = "get_status"


}