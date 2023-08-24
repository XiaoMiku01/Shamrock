package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import com.tencent.qqnt.msg.LogicException
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.getMsg() {
    getOrPost("/get_msg") {
        val msgId = fetchOrThrow("message_id")
        call.respondText(ActionManager["get_msg"]?.handle(ActionSession(mapOf(
            "message_id" to msgId
        ))) ?: throw LogicException("Unable to obtain get_msg handler."))
    }
}