package moe.protocol.service.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.fuqiuluo.remote.InternalWebSocketServer
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.GlobalJson
import moe.protocol.service.helper.ShamrockConfig

internal abstract class WebSocketPushServlet : BasePushServlet {
    override val address: String
        get() = ShamrockConfig.getWebHookAddress()

     override fun allowPush(): Boolean {
         return ShamrockConfig.openWebSocket()
     }

    protected inline fun <reified T> pushTo(body: T) {
        if(!allowPush()) return
        try {
            InternalWebSocketServer?.broadcastEvent(GlobalJson.encodeToString(body))
        } catch (e: Throwable) {
            LogCenter.log("WS推送失败: ${e.stackTraceToString()}", Level.ERROR)
        }
    }
 }