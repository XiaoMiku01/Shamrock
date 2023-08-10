package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.data.BotStatus
import moe.fuqiuluo.http.action.data.Self
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