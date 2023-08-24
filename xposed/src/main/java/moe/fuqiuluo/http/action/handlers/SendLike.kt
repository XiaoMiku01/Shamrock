package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.protocol.VisitorSvc
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.entries.EmptyObject

internal object SendLike: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val times = session.getIntOrNull("times") ?: return noParam("times")
        val uin = session.getLongOrNull("user_id") ?: return noParam("user_id")
        VisitorSvc.vote(uin, times)
        return ok(EmptyObject)
    }

    override fun path(): String = "send_like"
}