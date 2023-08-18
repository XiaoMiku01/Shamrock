package moe.fuqiuluo.http.api

import com.tencent.mobileqq.profilecard.api.IProfileCardBlacklistApi
import com.tencent.mobileqq.qroute.QRoute
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.tools.fetchGetOrThrow
import moe.fuqiuluo.xposed.tools.respond
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Routing.isBlackListUin() {
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