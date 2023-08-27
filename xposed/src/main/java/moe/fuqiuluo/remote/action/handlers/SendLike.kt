package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.VisitorSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.EmptyObject

internal object SendLike: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val times = session.getInt("times")
        val uin = session.getLong("user_id")
        VisitorSvc.vote(uin, times)
        return ok(EmptyObject)
    }

    override val requiredParams: Array<String> = arrayOf("times", "user_id")

    override fun path(): String = "send_like"
}