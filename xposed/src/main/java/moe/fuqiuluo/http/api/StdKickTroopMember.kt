package moe.fuqiuluo.http.api

import com.tencent.qqnt.msg.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.kickTroopMember() {
    getOrPost("/set_group_kick") {
        call.respondText(ActionManager["set_group_kick"]?.handle(ActionSession(mapOf(
            "user_id" to fetchOrThrow("user_id"),
            "group_id" to fetchOrThrow("group_id"),
            "reject_add_request" to (fetchOrNull("reject_add_request") ?: "false"),
        ))) ?: throw LogicException("Unable to obtain set_group_kick handler."))
    }
}