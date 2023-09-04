package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.service.data.SimpleTroopInfo
import moe.protocol.servlet.protocol.GroupSvc

internal object GetTroopInfo: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(groupId, refresh)
    }

    suspend operator fun invoke(groupId: String, refresh: Boolean): String {
        val groupInfo = GroupSvc.getGroupInfo(groupId, refresh)
        return if ( groupInfo == null || groupInfo.troopuin.isNullOrBlank()) {
            logic("Unable to obtain group information")
        } else {
            ok(
                SimpleTroopInfo(
                groupId = groupInfo.troopuin,
                groupUin = groupInfo.troopcode,
                groupName = groupInfo.troopname ?: groupInfo.newTroopName ?: groupInfo.oldTroopName,
                groupRemark = groupInfo.troopRemark,
                adminList = GroupSvc.getAdminList(groupId, true),
                classText = groupInfo.mGroupClassExtText,
                isFrozen = groupInfo.mIsFreezed != 0,
                maxMember = groupInfo.wMemberMax,
                memNum = groupInfo.wMemberNum,
                memCount = groupInfo.wMemberNum,
                maxNum = groupInfo.wMemberMax,
            )
            )
        }
    }

    override val requiredParams: Array<String> = arrayOf("group_id")

    override fun path(): String = "get_group_info"
}