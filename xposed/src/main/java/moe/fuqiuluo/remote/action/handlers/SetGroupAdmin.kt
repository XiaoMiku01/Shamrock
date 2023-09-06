package moe.fuqiuluo.remote.action.handlers

import moe.protocol.servlet.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SetGroupAdmin: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getLong("group_id")
        val userId = session.getLong("user_id")
        val enable = session.getBoolean("enable")
        return invoke(groupId, userId, enable, session.echo)
    }

    operator fun invoke(groupId: Long, userId: Long, enable: Boolean, echo: String = ""): String {
        if (!GroupSvc.isOwner(groupId.toString())) {
            return logic("you are not owner", echo)
        }
        GroupSvc.setGroupAdmin(groupId, userId, enable)
        return ok("成功", echo)
    }

    override fun path(): String = "set_group_admin"
}