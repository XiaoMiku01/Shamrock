package moe.fuqiuluo.remote.api

import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.profilecard.api.IProfileCardBlacklistApi
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.msg.LogicException
import com.tencent.qqnt.protocol.CardSvc
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Routing.userAction() {
    getOrPost("/get_stranger_info") {
        val uin = fetchOrThrow("user_id")
        val info = CardSvc.getProfileCard(uin)
        call.respond(mapOf(
            "user_id" to uin,
            "nickname" to info.strNick,
            "age" to info.age.toString(),
            "sex" to when(info.shGender) {
                Card.FEMALE -> "female"
                Card.MALE -> "male"
                else -> "unknown"
            }
        ))
    }

    get("/is_blacklist_uin") {
        val uin = fetchGetOrThrow("uin")
        val blacklistApi = QRoute.api(IProfileCardBlacklistApi::class.java)
        val isBlack = suspendCoroutine { continuation ->
            blacklistApi.isBlackOrBlackedUin(uin) {
                continuation.resume(it)
            }
        }
        respond(true, Status.Ok, isBlack)
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
        ))) ?: throw LogicException("Unable to obtain send_like handler."))
    }
}