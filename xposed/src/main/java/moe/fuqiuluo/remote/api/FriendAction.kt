package moe.fuqiuluo.remote.api

import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.profilecard.api.IProfileCardBlacklistApi
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.protocol.CardSvc
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.remote.action.handlers.GetFriendList
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.fetchOrNull
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Routing.friendAction() {
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

    getOrPost("/get_friend_list") {
        val refresh = fetchOrNull("refresh")?.toBoolean() ?: false
        call.respondText(GetFriendList(refresh))
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
}