package moe.fuqiuluo.http.api

import com.tencent.qqnt.msg.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.sendLike() {
    getOrPost("/send_like") {
        val uin = fetchOrThrow("user_id")
        val cnt = fetchOrThrow("times")
        call.respondText(ActionManager["send_like"]?.handle(ActionSession(mapOf(
            "user_id" to uin,
            "cnt" to cnt
        ))) ?: throw LogicException("Unable to obtain send_like handler."))
    }
}