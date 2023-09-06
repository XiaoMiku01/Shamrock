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
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.URI
import java.util.Collections


internal var InternalWebSocketServer: moe.fuqiuluo.remote.WebSocketServer? = null

internal class WebSocketServer(
    val port: Int
) : WebSocketServer(InetSocketAddress(port)) {
    private val eventReceivers = Collections.synchronizedList(mutableListOf<WebSocket>())

    fun broadcastEvent(text: String) {
        broadcast(text, eventReceivers)
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        val path = URI.create(handshake.resourceDescriptor).path
        if (path != "/api") {
            eventReceivers.add(conn)
        }
        LogCenter.log("WSServer连接(${conn.remoteSocketAddress.address.hostAddress}:${conn.remoteSocketAddress.port}$path)", Level.DEBUG)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        val path = URI.create(conn.resourceDescriptor).path
        if (path != "/api") {
            eventReceivers.remove(conn)
        }
        LogCenter.log("WSServer断开(${conn.remoteSocketAddress.address.hostAddress}:${conn.remoteSocketAddress.port}$path): $code,$reason,$remote", Level.DEBUG)
    }

    override fun onMessage(conn: WebSocket, message: String) {
        val path = URI.create(conn.resourceDescriptor).path
        GlobalScope.launch {
            onHandleAction(conn, message, path)
        }
    }

    private suspend fun onHandleAction(conn: WebSocket, message: String, path: String) {
        val respond = kotlin.runCatching {
            val actionObject = Json.parseToJsonElement(message).asJsonObject
            val action = actionObject["action"].asString
            val echo = actionObject["echo"].asStringOrNull ?: ""
            val params = actionObject["params"].asJsonObject

            val handler = ActionManager[action]
            handler?.handle(ActionSession(params, echo))
                ?: resultToString(false, Status.UnsupportedAction, EmptyObject, "不支持的Action", echo = echo)
        }.getOrNull()
        respond?.let { conn.send(it) }
    }

    override fun onError(conn: WebSocket, ex: Exception?) {
        LogCenter.log("WSServer Error: " + ex?.stackTraceToString(), Level.ERROR)
    }

    override fun onStart() {
        LogCenter.log("WSServer start running on ws://0.0.0.0:$port!")
    }
}