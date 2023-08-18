package moe.fuqiuluo.http.api

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.tools.fetchGetOrNull
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchPostOrNull
import moe.fuqiuluo.xposed.tools.fetchPostOrThrow
import moe.fuqiuluo.xposed.tools.respond

fun Routing.setProfileCard() {
    get("/set_qq_profile") {
        val nickName = fetchGetOrThrow("nickname")
        val company = fetchGetOrThrow("company")
        val email = fetchGetOrThrow("email")
        val college = fetchGetOrThrow("college")
        val personalNote = fetchGetOrThrow("personal_note")

        val age = fetchGetOrNull("age")
        val birthday = fetchGetOrNull("birthday")

        val handler = ActionManager["set_qq_profile"]!!
        handler.handle(ActionSession(mapOf(
            "nickname" to nickName,
            "company" to company,
            "email" to email,
            "college" to college,
            "personal_note" to personalNote,
            "age" to age,
            "birthday" to birthday
        )))

        respond(true, Status.Ok, "设置成功")
    }

    post("/set_qq_profile") {
        val nickName = fetchPostOrThrow("nickname")
        val company = fetchPostOrThrow("company")
        val email = fetchPostOrThrow("email")
        val college = fetchPostOrThrow("college")
        val personalNote = fetchPostOrThrow("personal_note")

        val age = fetchPostOrNull("age")?.toInt()
        val birthday = fetchPostOrNull("birthday")?.toInt()

        val handler = ActionManager["set_qq_profile"]!!
        handler.handle(ActionSession(mapOf(
            "nickname" to nickName,
            "company" to company,
            "email" to email,
            "college" to college,
            "personal_note" to personalNote,
            "age" to age,
            "birthday" to birthday
        )))

        respond(true, Status.Ok, "设置成功")
    }
}