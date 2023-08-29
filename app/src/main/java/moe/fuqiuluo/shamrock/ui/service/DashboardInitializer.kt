@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.shamrock.ui.service

import android.content.Context
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.fuqiuluo.remote.entries.CommonResult
import moe.fuqiuluo.remote.entries.CurrentAccount
import moe.fuqiuluo.shamrock.ui.app.AppRuntime.AccountInfo
import moe.fuqiuluo.shamrock.ui.app.AppRuntime.log
import moe.fuqiuluo.shamrock.ui.app.AppRuntime.state
import moe.fuqiuluo.shamrock.ui.app.Level
import moe.fuqiuluo.shamrock.ui.service.internal.broadcastToModule
import moe.fuqiuluo.xposed.tools.GlobalClient
import java.net.ConnectException
import java.util.Timer
import kotlin.concurrent.timer

object DashboardInitializer {
    private  var servicePort: Int = 0
    private lateinit var heartbeatTimer: Timer

    operator fun invoke(context: Context, port: Int) {
        servicePort = port
        initHeartbeat(true, context)
    }

    private fun initHeartbeat(reload: Boolean, context: Context) {
        if (::heartbeatTimer.isInitialized && !reload) {
            return
        }
        if (::heartbeatTimer.isInitialized) {
            heartbeatTimer.cancel()
        }
        heartbeatTimer = timer("heartbeat", false, 0, 1000L * 30) {
            checkService(context)
        }
    }

    private fun checkService(context: Context) {
        GlobalScope.launch {
            try {
                GlobalClient.get("http://127.0.0.1:$servicePort/get_account_info").let {
                    if (it.status == HttpStatusCode.OK) {
                        val result =
                            Json.decodeFromString<CommonResult<CurrentAccount>>(it.bodyAsText())
                        state.isFined.value = result.retcode == 0
                        if (result.retcode != 0) {
                            log("尝试从接口获取账号信息失败，未知错误。", Level.ERROR)
                        } else {
                            log("心跳请求发送已发送。")
                            AccountInfo.let { account ->
                                account.uin.value = result.data.uin.toString()
                                account.nick.value = result.data.nick
                            }
                        }
                    } else {
                        state.isFined.value = false
                        log("尝试从接口获取账号信息失败，服务运行异常。", Level.ERROR)
                    }
                }
            } catch (e: ConnectException) {
                state.isFined.value = false
                log("检测到Service死亡，正在尝试重新启动！")
                context.broadcastToModule {
                    putExtra("__cmd", "checkAndStartService")
                }
            } catch (e: Throwable) {
                state.isFined.value = false
                log(e.stackTraceToString(), Level.ERROR)
            }
        }
    }
}