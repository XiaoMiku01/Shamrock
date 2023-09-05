package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.servlet.protocol.GroupSvc

internal object GroupPoke: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val userId = session.getString("user_id")
        return invoke(groupId, userId)
    }

    operator fun invoke(groupId: String, userId: String): String {
        GroupSvc.poke(groupId, userId)
        return ok("成功")
    }

    override val requiredParams: Array<String> = arrayOf("group_id", "user_id")

    override fun path(): String = "poke"
}