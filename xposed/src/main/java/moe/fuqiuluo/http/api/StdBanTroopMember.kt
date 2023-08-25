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

fun Routing.banTroopMember() {
    getOrPost("/set_group_ban") {
        val groupId = fetchOrThrow("group_id") .toLong()
        val userId = fetchOrThrow("user_id") .toLong()
        val duration = fetchOrNull("duration")?.toInt() ?: (30 * 60)

        call.respondText(ActionManager["set_group_ban"]?.handle(ActionSession(mapOf(
            "user_id" to userId,
            "group_id" to groupId,
            "duration" to duration,
        ))) ?: throw LogicException("Unable to obtain set_group_ban handler."))
    }
}