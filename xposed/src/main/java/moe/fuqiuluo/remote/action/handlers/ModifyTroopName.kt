package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.EmptyObject

internal object ModifyTroopName: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val groupName = session.getString("group_name")

        return invoke(groupId, groupName)
    }

    operator fun invoke(groupId: String, name: String): String {
        return if (GroupSvc.isAdmin(groupId)) {
            GroupSvc.modifyTroopName(groupId, name)
            ok(EmptyObject)
        } else {
            logic("You are not the administrator of the group")
        }
    }

    override val requiredParams: Array<String> = arrayOf("group_id", "group_name")

    override fun path(): String = "set_group_name"
}