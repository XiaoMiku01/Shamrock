package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import com.tencent.qqnt.msg.LogicException
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchPostOrThrow

fun Routing.getMsg() {
    get("/get_msg") {
        val msgId = fetchGetOrThrow("message_id")
        call.respondText(ActionManager["get_msg"]?.handle(ActionSession(mapOf(
            "message_id" to msgId
        ))) ?: throw LogicException("Unable to obtain get_msg handler.")
        )
    }

    post("/get_msg") {
        val msgId = fetchPostOrThrow("message_id")
        call.respondText(ActionManager["get_msg"]?.handle(ActionSession(mapOf(
            "message_id" to msgId
        ))) ?: throw LogicException("Unable to obtain get_msg handler.")
        )
    }
}