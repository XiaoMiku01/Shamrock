package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler

internal object KickTroopMember: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val groupId = session.getLongOrNull("group_id")
            ?: return noParam("group_id")
        val userId = session.getLongOrNull("user_id")
            ?: return noParam("user_id")
        val rejectAddRequest = session.getBooleanOrDefault("reject_add_request", false)

        GroupSvc.kickMember(groupId, rejectAddRequest, userId)

        return ok("成功")
    }

    override fun path(): String = "set_group_kick"
}