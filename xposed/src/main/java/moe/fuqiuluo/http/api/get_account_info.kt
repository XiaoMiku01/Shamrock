package moe.fuqiuluo.http.api

import com.tencent.mobileqq.app.QQAppInterface
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.CurrentAccount
import moe.fuqiuluo.xposed.tools.getOrPost
import mqq.app.MobileQQ

fun Routing.getAccountInfo() {
    getOrPost("/get_account_info") {
        val accounts = MobileQQ.getMobileQQ().allAccounts
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val curUin = runtime.currentAccountUin
        val account = accounts.firstOrNull { it.uin == curUin }
        if (account == null || !account.isLogined) {
            this.call.respond(CommonResult("failed", 1, null, "当前不处于已登录状态"))
        } else {
            this.call.respond(CommonResult("ok", 0, CurrentAccount(
                curUin.toLong(), runtime.isLogin, if (runtime is QQAppInterface) runtime.currentNickname else "unknown"
            )))
        }
    }

    getOrPost("/get_history_account_info") {
        val accounts = MobileQQ.getMobileQQ().allAccounts
        val accountList = accounts.map {
            CurrentAccount(it.uin.toLong(), it.isLogined)
        }
        this.call.respond(CommonResult("ok", 0, accountList))
    }
}