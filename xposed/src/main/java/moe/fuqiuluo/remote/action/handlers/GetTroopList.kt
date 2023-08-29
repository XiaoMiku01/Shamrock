package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopInfo
import com.tencent.qqnt.protocol.GroupSvc

internal object GetTroopList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(refresh)
    }

    suspend operator fun invoke(refresh: Boolean): String {
        val troopList = GroupSvc.getGroupList(refresh) ?: return logic("unable to get group list")
        return ok(arrayListOf<SimpleTroopInfo>().apply {
            troopList.forEach { groupInfo ->
                add(SimpleTroopInfo(
                    groupId = groupInfo.troopuin,
                    groupUin = groupInfo.troopcode,
                    groupName = groupInfo.troopname ?: groupInfo.newTroopName ?: groupInfo.oldTroopName,
                    groupRemark = groupInfo.troopRemark,
                    adminList = GroupSvc.getAdminList(groupInfo.troopuin, true),
                    classText = groupInfo.mGroupClassExtText,
                    isFrozen = groupInfo.mIsFreezed != 0,
                    maxMember = groupInfo.wMemberMax,
                    memNum = groupInfo.wMemberNum,
                    memCount = groupInfo.wMemberNum,
                    maxNum = groupInfo.wMemberMax,
                ))
            }
        })
    }

    override fun path(): String = "get_group_list"
}