@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.shamrock.ui.service

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.fuqiuluo.remote.entries.CommonResult
import moe.fuqiuluo.remote.entries.CurrentAccount
import moe.fuqiuluo.shamrock.ui.app.AppRuntime
import moe.fuqiuluo.shamrock.ui.app.Level
import moe.fuqiuluo.xposed.tools.GlobalClient

object DashboardInitializer {
    operator fun invoke(port: Int) {
        GlobalScope.launch {
            try {
                GlobalClient.get("http://127.0.0.1:$port/get_account_info").let {
                    if (it.status == HttpStatusCode.OK) {
                        val result = Json.decodeFromString<CommonResult<CurrentAccount>>(it.bodyAsText())
                        if (result.retcode != 0) {
                            AppRuntime.log("尝试从接口获取账号信息失败，未知错误。", Level.ERROR)
                        } else {
                            AppRuntime.state.isFined.value = true
                            AppRuntime.AccountInfo.let {
                                it.uin.value = result.data.uin.toString()
                                it.nick.value = result.data.nick
                            }
                        }
                    } else {
                        AppRuntime.log("尝试从接口获取账号信息失败，请检查是否存在端口冲突。", Level.ERROR)
                    }
                }
            } catch (e: Throwable) {
                AppRuntime.log(e.stackTraceToString(), Level.ERROR)
            }
        }
    }
}