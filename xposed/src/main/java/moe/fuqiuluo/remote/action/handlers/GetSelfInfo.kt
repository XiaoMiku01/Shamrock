package moe.fuqiuluo.remote.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.service.data.UserDetail
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import mqq.app.MobileQQ

internal object GetSelfInfo: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        //val accounts = MobileQQ.getMobileQQ().allAccounts
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val curUin = runtime.currentAccountUin
        //val account = accounts.firstOrNull { it.uin == curUin }

        return resultToString(true, Status.Ok, UserDetail(
            curUin, runtime.currentNickname, runtime.currentNickname
        ), echo = session.echo)
    }

    override fun path(): String = "get_self_info"
}