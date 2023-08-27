@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.remote.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.data.troop.TroopInfo
import com.tencent.mobileqq.troop.api.ITroopInfoService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopInfo
import com.tencent.qqnt.protocol.GroupSvc
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object GetTroopList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val refresh = session.getBooleanOrDefault("refresh", false)

        val service = runtime
            .getRuntimeService(ITroopInfoService::class.java, "all")

        var troopList = service.allTroopList
        if(refresh || !service.isTroopCacheInited || troopList == null) {
            if(!requestGroupList(service, troopList)) {
                return logic("Unable to obtain group list")
            } else {
                troopList = service.allTroopList
            }
        }

        return ok(arrayListOf<SimpleTroopInfo>().apply {
            troopList?.forEach {  groupInfo ->
                add(SimpleTroopInfo(
                    groupId = groupInfo.troopuin,
                    groupUin = groupInfo.troopcode,
                    groupName = groupInfo.troopname ?: groupInfo.newTroopName ?: groupInfo.oldTroopName,
                    groupRemark = groupInfo.troopRemark,
                    adminList = GroupSvc.getAdminList(groupInfo.troopuin, true),
                    classText = groupInfo.mGroupClassExtText,
                    isFrozen = groupInfo.mIsFreezed != 0,
                    maxMember = groupInfo.wMemberMax,
                    memNum = groupInfo.wMemberNum
                ))
            }
        })
    }

    private suspend fun requestGroupList(
        service: ITroopInfoService,
        troopList: List<TroopInfo>?
    ): Boolean {
        //if (!troopList.isNullOrEmpty()) {
        //    // 删除缓存 重新获取
        //    service.deleteTroopList(troopList.map { it.troopuin })
        //}
        GroupSvc.refreshTroopList()

        return suspendCancellableCoroutine { continuation ->
            val waiter = GlobalScope.launch {
                do {
                    delay(1000)
                } while (
                    !service.isTroopCacheInited
                    // || (!troopList.isNullOrEmpty() && service.hasNoTroop()) 判断不合理
                )
                continuation.resume(true)
            }
            continuation.invokeOnCancellation {
                waiter.cancel()
                continuation.resume(false)
            }
        }
    }

    override fun path(): String = "get_group_list"
}