package moe.fuqiuluo.remote.action.handlers

import moe.protocol.servlet.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object ModifyTroopMemberName: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val userId = session.getString("user_id")
        val name = session.getStringOrNull("card") ?: ""
        return invoke(groupId, userId, name, session.echo)
    }

    operator fun invoke(groupId: String, userId: String, card: String, echo: String = ""): String {
        if (!GroupSvc.isAdmin(groupId)) {
            return logic("you are not admin", echo)
        }
        return if(GroupSvc.modifyGroupMemberCard(groupId.toLong(), userId.toLong(), card))
            ok("成功", echo)
        else error("check if member or group exist")
    }

    override val requiredParams: Array<String> = arrayOf("group_id", "user_id")

    override fun path(): String = "set_group_card"
}