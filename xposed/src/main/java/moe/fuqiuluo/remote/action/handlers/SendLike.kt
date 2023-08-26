package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.VisitorSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.EmptyObject

internal object SendLike: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val times = session.getIntOrNull("times") ?: return noParam("times")
        val uin = session.getLongOrNull("user_id") ?: return noParam("user_id")
        VisitorSvc.vote(uin, times)
        return ok(EmptyObject)
    }

    override fun path(): String = "send_like"
}