package moe.protocol.service.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.fuqiuluo.remote.InternalWebSocketServer
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.protocol.service.helper.ShamrockConfig

private val json = Json {
    ignoreUnknownKeys = true // 忽略未知key
    isLenient = true // 宽松模式
    allowSpecialFloatingPointValues = true // 允许特殊浮点数值（如NaN）
    encodeDefaults = false // 不编码默认值
    prettyPrint = true // 格式化输出
    coerceInputValues = true // 强制输入值
}

internal abstract class WebSocketPushServlet : BasePushServlet {
    override val address: String
        get() = ShamrockConfig.getWebHookAddress()

     override fun allowPush(): Boolean {
         return ShamrockConfig.openWebSocket()
     }

    protected inline fun <reified T> pushTo(body: T) {
        if(!allowPush()) return
        try {
            InternalWebSocketServer?.broadcastEvent(json.encodeToString(body))
        } catch (e: Throwable) {
            LogCenter.log("WS推送失败: ${e.stackTraceToString()}", Level.ERROR)
        }
    }
 }