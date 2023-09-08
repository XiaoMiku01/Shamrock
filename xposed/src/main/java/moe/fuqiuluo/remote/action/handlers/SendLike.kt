package moe.fuqiuluo.remote.action.handlers

import moe.protocol.servlet.VisitorSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SendLike: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val times = session.getInt("times")
        val uin = session.getLong("user_id")
        VisitorSvc.vote(uin, times)
        return ok("成功", session.echo)
    }

    override val requiredParams: Array<String> = arrayOf("times", "user_id")

    override fun path(): String = "send_like"
}