package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.UserDetail
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import mqq.app.MobileQQ

internal object GetSelfInfo: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        //val accounts = MobileQQ.getMobileQQ().allAccounts
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val curUin = runtime.currentAccountUin
        //val account = accounts.firstOrNull { it.uin == curUin }

        return resultToString(true, Status.Ok, UserDetail(
            curUin, runtime.currentNickname, runtime.currentNickname
        )
        )
    }

    override fun path(): String = "get_self_info"
}