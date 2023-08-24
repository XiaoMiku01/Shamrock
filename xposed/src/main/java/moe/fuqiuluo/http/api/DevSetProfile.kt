package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.setProfileCard() {
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
}