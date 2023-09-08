@file:OptIn(DelicateCoroutinesApi::class)

package moe.fuqiuluo.remote

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.entries.EmptyObject
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

internal var InternalWebSocketClient: moe.fuqiuluo.remote.WebSocketClient? = null

class WebSocketClient(url: String, wsHeaders: Map<String, String>): WebSocketClient(URI("ws://$url"), wsHeaders) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        LogCenter.log("WebSocketClient onOpen: ${handshakedata?.httpStatus}, ${handshakedata?.httpStatusMessage}")
    }

    override fun onMessage(message: String) {
        GlobalScope.launch {
            handleMessage(message)
        }
    }

    private suspend fun handleMessage(message: String) {
        val respond = kotlin.runCatching {
            val actionObject = Json.parseToJsonElement(message).asJsonObject
            val action = actionObject["action"].asString
            val echo = actionObject["echo"].asStringOrNull ?: ""
            val params = actionObject["params"].asJsonObject

            val handler = ActionManager[action]
            handler?.handle(ActionSession(params, echo))
                ?: resultToString(false, Status.UnsupportedAction, EmptyObject, "不支持的Action", echo = echo)
        }.getOrNull()
        respond?.let { send(it) }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        LogCenter.log("WebSocketClient onClose: $code, $reason, $remote")
    }

    override fun onError(ex: Exception?) {
        LogCenter.log("WebSocketClient onError: ${ex?.message}")
    }
}