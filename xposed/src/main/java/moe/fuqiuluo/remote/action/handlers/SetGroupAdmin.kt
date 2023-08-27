package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SetGroupAdmin: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getLong("group_id")
        val userId = session.getLong("user_id")
        val enable = session.getBoolean("enable")
        return invoke(groupId, userId, enable)
    }

    operator fun invoke(groupId: Long, userId: Long, enable: Boolean): String {
        if (!GroupSvc.isOwner(groupId.toString())) {
            return logic("you are not owner")
        }
        GroupSvc.setGroupAdmin(groupId, userId, enable)
        return ok("设置成功")
    }

    override fun path(): String = "set_group_admin"
}