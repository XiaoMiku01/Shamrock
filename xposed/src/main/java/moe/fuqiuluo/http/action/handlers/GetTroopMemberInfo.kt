package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopMemberInfo
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty

internal object GetTroopMemberInfo: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        if (!session.has("user_id")) {
            return noParam("user_id")
        }
        val uin = session.getString("user_id")
        if (!session.has("group_id")) {
            return noParam("group_id")
        }
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)

        val info = GroupSvc.getTroopMemberInfoByUin(groupId, uin, refresh)
            ?: return logic("cannot get troop member info")

        return ok(
            SimpleTroopMemberInfo(
            uin = info.memberuin,
            name = info.friendnick.ifNullOrEmpty(info.autoremark) ?: "",
            showName = info.troopnick.ifNullOrEmpty(info.troopColorNick),
            distance = info.distance,
            honor = (info.honorList ?: "")
                .split("|")
                .filter { it.isNotBlank() }
                .map { it.toInt() },
            joinTime = info.join_time,
            lastActiveTime = info.last_active_time,
            uniqueName = info.mUniqueTitle
        )
        )
    }

    override fun path(): String = "get_group_member_info"
}

