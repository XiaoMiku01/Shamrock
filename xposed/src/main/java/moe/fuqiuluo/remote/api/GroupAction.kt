package moe.fuqiuluo.remote.api

import com.tencent.qqnt.msg.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.handlers.BanTroopMember
import moe.fuqiuluo.remote.action.handlers.SetGroupWholeBan
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.troopAction() {
    getOrPost("/set_group_whole_ban") {
        val groupId = fetchOrThrow("group_id") .toLong()
        val enable = fetchOrThrow("user_id").toBooleanStrict()
        call.respondText(SetGroupWholeBan(groupId, enable))
    }

    getOrPost("/set_group_ban") {
        val groupId = fetchOrThrow("group_id") .toLong()
        val userId = fetchOrThrow("user_id") .toLong()
        val duration = fetchOrNull("duration")?.toInt() ?: (30 * 60)

        call.respondText(BanTroopMember(groupId, userId, duration))
    }

    getOrPost("/set_group_kick") {
        call.respondText(ActionManager["set_group_kick"]?.handle(ActionSession(mapOf(
            "user_id" to fetchOrThrow("user_id"),
            "group_id" to fetchOrThrow("group_id"),
            "reject_add_request" to (fetchOrNull("reject_add_request") ?: "false"),
        ))) ?: throw LogicException("Unable to obtain set_group_kick handler."))
    }
}