package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object LeaveTroop: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        return invoke(groupId)
    }

    operator fun invoke(groupId: String): String {
        if (GroupSvc.isOwner(groupId)) {
            return error("you are the owner of this group")
        }
        GroupSvc.resignTroop(groupId.toLong())
        return ok("成功")
    }

    override val requiredParams: Array<String> = arrayOf("group_id")

    override fun path(): String = "leave_group"
}