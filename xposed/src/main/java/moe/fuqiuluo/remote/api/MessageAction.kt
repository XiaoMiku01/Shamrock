package moe.fuqiuluo.remote.api

import com.tencent.qqnt.msg.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.xposed.tools.fetchGetOrNull
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.fetchPostJsonArray
import moe.fuqiuluo.xposed.tools.fetchPostOrNull
import moe.fuqiuluo.xposed.tools.fetchPostOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.isJsonData
import moe.fuqiuluo.xposed.tools.isString

fun Routing.messageAction() {
    getOrPost("/get_msg") {
        val msgId = fetchOrThrow("message_id")
        call.respondText(
            ActionManager["get_msg"]?.handle(
                ActionSession(mapOf(
            "message_id" to msgId
        ))) ?: throw LogicException("Unable to obtain get_msg handler."))
    }

    get("/send_msg") {
        val msgType = fetchGetOrThrow("message_type")
        val message = fetchGetOrThrow("message")
        val autoEscape = fetchGetOrNull("auto_escape")?.toBoolean() ?: false
        val peerIdKey = if(msgType == "group") "group_id" else "user_id"
        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to msgType,
            peerIdKey to fetchGetOrThrow(peerIdKey),
            "message" to message,
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
    }

    post("/send_msg") {
        val msgType = fetchPostOrThrow("message_type")
        val autoEscape = fetchPostOrNull("auto_escape")?.toBoolean() ?: false
        val peerIdKey = if(msgType == "group") "group_id" else "user_id"
        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to msgType,
            peerIdKey to fetchPostOrThrow(peerIdKey),
            "message" to if (isJsonData() && !isString("message")) {
                fetchPostJsonArray("message")
            } else fetchPostOrThrow("message"),
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
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
        ))) ?: throw LogicException("Unable to obtain send_message handler.")
        )
    }

    get("/send_private_msg") {
        val userId = fetchGetOrThrow("user_id")
        val message = fetchGetOrThrow("message")
        val autoEscape = fetchGetOrNull("auto_escape")?.toBoolean() ?: false

        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to "private",
            "user_id" to userId,
            "message" to message,
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
    }

    post("/send_private_msg") {
        val userId = fetchPostOrThrow("user_id")
        val autoEscape = fetchPostOrNull("auto_escape")?.toBoolean() ?: false
        call.respondText(ActionManager["send_message"]?.handle(ActionSession(mapOf(
            "detail_type" to "private",
            "user_id" to userId,
            "message" to if (isJsonData() && !isString("message")) {
                fetchPostJsonArray("message")
            } else fetchPostOrThrow("message"),
            "auto_escape" to autoEscape
        ))) ?: throw LogicException("Unable to obtain send_message handler."))
    }
}