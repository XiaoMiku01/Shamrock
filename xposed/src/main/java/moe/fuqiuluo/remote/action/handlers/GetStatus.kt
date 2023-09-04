package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.action.ActionSession
import moe.protocol.service.data.BotStatus
import moe.protocol.service.data.Self
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import mqq.app.MobileQQ

internal object GetStatus: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
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