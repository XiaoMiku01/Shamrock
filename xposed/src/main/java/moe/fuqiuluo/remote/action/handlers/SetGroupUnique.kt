package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SetGroupUnique: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val userId = session.getString("user_id")
        val unique = session.getString("special_title")
        return invoke(groupId, userId, unique)
    }

    suspend operator fun invoke(groupId: String, userId: String, unique: String): String {
        if (!GroupSvc.isAdmin(groupId)) {
            return error("you are not admin")
        }
        GroupSvc.setGroupUniqueTitle(groupId, userId, unique)
        return ok("成功")
    }

    override val requiredParams: Array<String> = arrayOf("group_id", "user_id", "special_title")


    override fun path(): String = "set_group_special_title"
}