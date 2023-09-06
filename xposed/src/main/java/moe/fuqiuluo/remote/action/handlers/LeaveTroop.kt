package moe.fuqiuluo.remote.action.handlers

import moe.protocol.servlet.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object LeaveTroop: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        return invoke(groupId, session.echo)
    }

    operator fun invoke(groupId: String, echo: String = ""): String {
        if (GroupSvc.isOwner(groupId)) {
            return error("you are the owner of this group", echo)
        }
        GroupSvc.resignTroop(groupId.toLong())
        return ok("成功", echo)
    }

    override val requiredParams: Array<String> = arrayOf("group_id")

    override fun path(): String = "leave_group"
}