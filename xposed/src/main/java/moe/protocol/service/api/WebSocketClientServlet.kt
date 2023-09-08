package moe.protocol.service.api

import kotlinx.serialization.encodeToString
import moe.fuqiuluo.remote.InternalWebSocketClient
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.GlobalJson
import moe.protocol.service.config.ShamrockConfig

internal abstract class WebSocketClientServlet : BasePushServlet {
    override val address: String
        get() = ShamrockConfig.getWebSocketClientAddress()

    override fun allowPush(): Boolean {
        return ShamrockConfig.openWebSocketClient()
    }

    protected inline fun <reified T> pushTo(body: T) {
        if(!allowPush()) return
        try {
            InternalWebSocketClient?.send(GlobalJson.encodeToString(body))
        } catch (e: Throwable) {
            LogCenter.log("被动WS推送失败: ${e.stackTraceToString()}", Level.ERROR)
        }
    }
}