package moe.fuqiuluo.remote.api

import moe.protocol.servlet.helper.LogicException
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.handlers.CleanCache
import moe.fuqiuluo.remote.action.handlers.GetModelShow
import moe.fuqiuluo.remote.action.handlers.RestartMe
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.userAction() {
    getOrPost("/get_model_show") {
        val uin = fetchOrNull("user_id")
        call.respondText(GetModelShow.invoke(uin?.toLong() ?: 0))
    }

    getOrPost("/clean_cache") {
        call.respondText(CleanCache())
    }

    getOrPost("/set_restart") {
        call.respondText(RestartMe(2000))
    }

    getOrPost("/set_qq_profile") {
        val nickName = fetchOrThrow("nickname")
        val company = fetchOrThrow("company")
        val email = fetchOrThrow("email")
        val college = fetchOrThrow("college")
        val personalNote = fetchOrThrow("personal_note")

        val age = fetchOrNull("age")
        val birthday = fetchOrNull("birthday")

        val handler = ActionManager["set_qq_profile"]!!

        call.respondText(handler.handle(ActionSession(mapOf(
            "nickname" to nickName,
            "company" to company,
            "email" to email,
            "college" to college,
            "personal_note" to personalNote,
            "age" to age,
            "birthday" to birthday
        ))))
    }

    getOrPost("/send_like") {
        val uin = fetchOrThrow("user_id")
        val cnt = fetchOrThrow("times")
        call.respondText(ActionManager["send_like"]?.handle(ActionSession(mapOf(
            "user_id" to uin,
            "cnt" to cnt
        ))) ?: throw LogicException("Unable to obtain send_like handler.")
        )
    }
}