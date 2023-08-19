package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.helper.msg.LogicException
import moe.fuqiuluo.xposed.tools.fetchGetOrNull
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchPostJsonArray
import moe.fuqiuluo.xposed.tools.fetchPostOrNull
import moe.fuqiuluo.xposed.tools.fetchPostOrThrow
import moe.fuqiuluo.xposed.tools.isJsonData
import moe.fuqiuluo.xposed.tools.isString

fun Routing.sendGroupMessage() {
    get("/send_msg") {


    }

    get("/send_group_msg") {
        val groupId = fetchGetOrThrow("group_id")
        val message = fetchGetOrThrow("message")
        val autoEscape = fetchGetOrNull("auto_escape")?.toBoolean() ?: false

        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to "group",
            "group_id" to groupId,
            "message" to message,
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
    }

    post("/send_group_msg") {
        val groupId = fetchPostOrThrow("group_id")
        val autoEscape = fetchPostOrNull("auto_escape")?.toBoolean() ?: false
        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to "group",
            "group_id" to groupId,
            "message" to if (isJsonData() && !isString("message")) {
                fetchPostJsonArray("message")
            } else fetchPostOrThrow("message"),
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
    }
}