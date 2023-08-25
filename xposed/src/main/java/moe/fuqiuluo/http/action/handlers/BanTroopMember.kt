package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler

internal object BanTroopMember: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val groupId = session.getLongOrNull("group_id")
            ?: return noParam("group_id")
        val userId = session.getLongOrNull("user_id")
            ?: return noParam("user_id")
        val duration = session.getIntOrNull("duration") ?: (30 * 60)

        GroupSvc.banMember(groupId, userId, duration)

        return ok("成功")
    }

    override fun path(): String = "set_group_ban"
}