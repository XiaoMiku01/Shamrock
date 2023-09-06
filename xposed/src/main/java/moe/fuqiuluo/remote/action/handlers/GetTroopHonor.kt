package moe.fuqiuluo.remote.action.handlers

import moe.protocol.service.data.GroupAllHonor
import moe.protocol.service.data.GroupMemberHonor
import com.tencent.mobileqq.data.*
import moe.protocol.servlet.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.service.data.HONOR_GROUP_FIRE
import moe.protocol.service.data.HONOR_GROUP_FLAME
import moe.protocol.service.data.HONOR_HAPPY
import moe.protocol.service.data.HONOR_NEWBIE
import moe.protocol.service.data.HONOR_TALKATIVE

internal object GetTroopHonor: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(groupId, refresh, session.echo)
    }

    suspend operator fun invoke(groupId: String, refresh: Boolean, echo: String = ""): String {
        val memberList = GroupSvc.getGroupMemberList(groupId, refresh)
            ?: return error("unable to fetch group member list", echo)
        val honorInfo = ArrayList<GroupMemberHonor>()
        memberList.forEach { member ->
            GroupSvc.parseHonor(member.honorList).forEach {
                val honor = nativeDecodeHonor(member.memberuin, it, member.mHonorRichFlag)
                if (honor != null) {
                    honor.nick = member.troopnick.ifBlank { member.friendnick }
                    honorInfo.add(honor)
                }
            }
        }

        return ok(GroupAllHonor(
            groupId = groupId,
            currentTalkActive = honorInfo.firstOrNull {
                it.id == HONOR_TALKATIVE
            },
            talkativeList = honorInfo.filter { it.id == HONOR_TALKATIVE },
            performerList = honorInfo.filter { it.id == HONOR_GROUP_FIRE },
            legendList = honorInfo.filter { it.id == HONOR_GROUP_FLAME },
            strongNewbieList = honorInfo.filter { it.id == HONOR_NEWBIE },
            emotionList = honorInfo.filter { it.id == HONOR_HAPPY },
            all = honorInfo
        ), echo)
    }

    override val requiredParams: Array<String> = arrayOf("group_id", "refresh")

    override fun path(): String = "get_group_honor_info"

    private external fun nativeDecodeHonor(userId: String, honorId: Int, honorFlag: Byte): GroupMemberHonor?
}