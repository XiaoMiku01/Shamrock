package moe.fuqiuluo.http.api

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import moe.fuqiuluo.xposed.tools.fetchGetOrNull
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchPostJsonObject
import moe.fuqiuluo.xposed.tools.fetchPostOrNull
import moe.fuqiuluo.xposed.tools.fetchPostOrThrow
import moe.fuqiuluo.xposed.tools.isJsonData

fun Routing.sendGroupMessage() {
    get("/send_group_msg") {
        val groupId = fetchGetOrThrow("group_id")
        val message = fetchGetOrThrow("message")
        val autoEscape = fetchGetOrNull("auto_escape")?.toBoolean() ?: false



    }

    post("/send_group_msg") {
        val groupId = fetchPostOrThrow("group_id")
        val autoEscape = fetchPostOrNull("auto_escape")?.toBoolean() ?: false
        if (isJsonData()) {
            val message = fetchPostJsonObject("message")

        } else {
            val message = fetchPostOrThrow("message")

        }
    }
}