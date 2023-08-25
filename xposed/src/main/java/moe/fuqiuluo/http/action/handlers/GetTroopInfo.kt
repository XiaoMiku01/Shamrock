package moe.fuqiuluo.http.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.data.troop.TroopInfo
import com.tencent.mobileqq.troop.api.ITroopInfoService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopInfo
import com.tencent.qqnt.protocol.GroupSvc
import mqq.app.MobileQQ

internal object GetTroopInfo: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        if (!session.has("group_id")) {
            return noParam("group_id")
        }
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val service = runtime
            .getRuntimeService(ITroopInfoService::class.java, "all")

        var groupInfo = GroupSvc.getGroupInfo(groupId)
        if(refresh || !service.isTroopCacheInited || groupInfo.troopuin.isNullOrBlank()) {
            val groupUin = kotlin.runCatching {
                groupId.toLong()
            }.onFailure {
                return badParam("The group_id format is illegal")
            }.getOrThrow()
            groupInfo = GroupSvc.requestGroupList(service, groupUin)
                ?: return logic("Unable to obtain group information")
        }

        return if ( !service.isTroopCacheInited || groupInfo.troopuin.isNullOrBlank()) {
            logic("Unable to obtain group information")
        } else {
            ok(SimpleTroopInfo(
                groupId = groupInfo.troopuin,
                groupUin = groupInfo.troopcode,
                groupName = groupInfo.troopname ?: groupInfo.newTroopName ?: groupInfo.oldTroopName,
                groupRemark = groupInfo.troopRemark,
                adminList = GroupSvc.getAdminList(groupId, true),
                classText = groupInfo.mGroupClassExtText,
                isFrozen = groupInfo.mIsFreezed != 0,
                maxMember = groupInfo.wMemberMax,
                memNum = groupInfo.wMemberNum
            ))
        }
    }

    override fun path(): String = "get_group_info"
}