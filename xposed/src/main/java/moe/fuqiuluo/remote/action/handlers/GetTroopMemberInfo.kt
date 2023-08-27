package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopMemberInfo
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty

internal object GetTroopMemberInfo: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val uin = session.getString("user_id")
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)

        val info = GroupSvc.getTroopMemberInfoByUin(groupId, uin, refresh)
            ?: return logic("cannot get troop member info")

        return ok(SimpleTroopMemberInfo(
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
        ))
    }

    override val requiredParams: Array<String> = arrayOf("user_id", "group_id")

    override fun path(): String = "get_group_member_info"
}

