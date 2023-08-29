package moe.fuqiuluo.remote.action.handlers

import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.data.MemberRole
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopMemberInfo
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty

internal object GetTroopMemberList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(groupId, refresh)
    }

    suspend operator fun invoke(groupId: String, refresh: Boolean): String {
        val memberList = GroupSvc.getGroupMemberList(groupId.toLong(), refresh)
            ?: return error("unable to get group member list")
        return ok(arrayListOf<SimpleTroopMemberInfo>().apply {
            memberList.forEach {  info ->
                if (info.memberuin != "0") {
                    add(SimpleTroopMemberInfo(
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
                        uniqueName = info.mUniqueTitle,
                        groupId = groupId,
                        nick = info.friendnick.ifNullOrEmpty(info.autoremark) ?: "",
                        sex = when(info.sex.toShort()) {
                            Card.FEMALE -> "female"
                            Card.MALE -> "male"
                            else -> "unknown"
                        },
                        area = info.alias ?: "",
                        lastSentTime = info.last_active_time,
                        level = info.level,
                        role = when {
                            GroupSvc.getOwner(groupId).toString() == info.memberuin -> MemberRole.Owner
                            info.memberuin.toLong() in GroupSvc.getAdminList(groupId) -> MemberRole.Admin
                            else -> MemberRole.Member
                        },
                        unfriendly = false,
                        title = info.mUniqueTitle ?: "",
                        titleExpireTime = info.mUniqueTitleExpire,
                        cardChangeable = GroupSvc.isAdmin(groupId)
                    ))
                }
            }
        })
    }

    override val requiredParams: Array<String> = arrayOf("group_id")

    override fun path(): String = "get_group_member_list"
}

