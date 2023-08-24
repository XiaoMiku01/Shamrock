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

        var groupInfo = service.getTroopInfo(groupId)
        if(refresh || !service.isTroopCacheInited || groupInfo.troopuin.isNullOrBlank()) {
            val groupUin = kotlin.runCatching {
                groupId.toLong()
            }.onFailure {
                return badParam("The group_id format is illegal")
            }.getOrThrow()
            groupInfo = requestGroupList(service, groupUin)
        }

        if (groupInfo == null || groupInfo.troopuin.isNullOrBlank()) {
            return logic("Unable to obtain group information")
        } else {
            return ok(
                SimpleTroopInfo(
                groupId = groupInfo.troopuin,
                groupUin = groupInfo.troopcode,
                groupName = groupInfo.troopname ?: groupInfo.newTroopName ?: groupInfo.oldTroopName,
                groupRemark = groupInfo.troopRemark,
                adminList = groupInfo.Administrator
                    .split("|", ",")
                    .map { it }
                    .apply { (this as ArrayList<String>).add(0, groupInfo.troopowneruin ?: "0") },
                classText = groupInfo.mGroupClassExtText,
                isFrozen = groupInfo.mIsFreezed != 0,
                //troopLevel = groupInfo.newTroopLevelMap,
                maxMember = groupInfo.wMemberMax,
                memNum = groupInfo.wMemberNum
            )
            )
        }
    }

    private suspend fun requestGroupList(dataService: ITroopInfoService, uin: Long): TroopInfo? {
        val strUin = uin.toString()
        return withTimeoutOrNull(5000) {
            var troopInfo: TroopInfo?
            do {
                troopInfo = dataService.getTroopInfo(strUin)
                delay(200)
            } while (troopInfo == null || troopInfo.troopuin.isNullOrBlank())
            return@withTimeoutOrNull troopInfo
        }
    }

    override fun path(): String = "get_group_info"
}