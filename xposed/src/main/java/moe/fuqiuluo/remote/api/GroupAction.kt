package moe.fuqiuluo.remote.api

import com.tencent.qqnt.helper.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.handlers.BanTroopMember
import moe.fuqiuluo.remote.action.handlers.GetTroopHonor
import moe.fuqiuluo.remote.action.handlers.GetTroopInfo
import moe.fuqiuluo.remote.action.handlers.GetTroopList
import moe.fuqiuluo.remote.action.handlers.GetTroopMemberInfo
import moe.fuqiuluo.remote.action.handlers.GetTroopMemberList
import moe.fuqiuluo.remote.action.handlers.ModifyTroopMemberName
import moe.fuqiuluo.remote.action.handlers.ModifyTroopName
import moe.fuqiuluo.remote.action.handlers.SetGroupAdmin
import moe.fuqiuluo.remote.action.handlers.SetGroupUnique
import moe.fuqiuluo.remote.action.handlers.SetGroupWholeBan
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.troopAction() {
    getOrPost("/get_group_honor_info") {
        val groupId = fetchOrThrow("group_id")
        val refresh = fetchOrNull("refresh")?.toBooleanStrict() ?: false
        call.respondText(GetTroopHonor(groupId, refresh))
    }

    getOrPost("/get_group_member_list") {
        val groupId = fetchOrThrow("group_id")
        val refresh = fetchOrNull("refresh")?.toBooleanStrict() ?: false
        call.respondText(GetTroopMemberList(groupId, refresh))
    }

    getOrPost("/get_group_member_info") {
        val groupId = fetchOrThrow("group_id")
        val userId = fetchOrThrow("user_id")
        val refresh = fetchOrNull("no_cache")?.toBooleanStrict() ?: false
        call.respondText(GetTroopMemberInfo(groupId, userId, refresh))
    }

    getOrPost("/get_group_list") {
        val refresh = fetchOrNull("refresh")?.toBooleanStrict() ?: false
        call.respondText(GetTroopList(refresh))
    }

    getOrPost("/get_group_info") {
        val groupId = fetchOrThrow("group_id")
        val refresh = fetchOrNull("no_cache")?.toBooleanStrict() ?: false
        call.respondText(GetTroopInfo(groupId, refresh))
    }

    getOrPost("/set_group_special_title") {
        val groupId = fetchOrThrow("group_id")
        val userId = fetchOrThrow("user_id")
        val title = fetchOrThrow("special_title")
        call.respondText(SetGroupUnique(groupId, userId, title))
    }

    getOrPost("/set_group_name") {
        val groupId = fetchOrThrow("group_id")
        val card = fetchOrThrow("group_name")
        call.respondText(ModifyTroopName(groupId, card))
    }

    getOrPost("/set_group_card") {
        val groupId = fetchOrThrow("group_id")
        val userId = fetchOrThrow("user_id")
        val card = fetchOrNull("card") ?: ""
        call.respondText(ModifyTroopMemberName(groupId, userId, card))
    }

    getOrPost("/set_group_admin") {
        val groupId = fetchOrThrow("group_id") .toLong()
        val userId = fetchOrThrow("user_id") .toLong()
        val enable = fetchOrThrow("enable").toBooleanStrict()
        call.respondText(SetGroupAdmin(groupId, userId, enable))
    }

    getOrPost("/set_group_whole_ban") {
        val groupId = fetchOrThrow("group_id") .toLong()
        val enable = fetchOrThrow("enable").toBooleanStrict()
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
        ))) ?: throw LogicException("Unable to obtain set_group_kick handler.")
        )
    }
}