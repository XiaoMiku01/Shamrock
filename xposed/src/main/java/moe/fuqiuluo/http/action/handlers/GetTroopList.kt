@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.http.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.troop.api.ITroopInfoService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.data.SimpleTroopInfo
import moe.fuqiuluo.http.action.helper.TroopRequestHelper
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object GetTroopList: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val service = runtime
            .getRuntimeService(ITroopInfoService::class.java, "all")

        var troopList = service.allTroopList
        if(!service.isTroopCacheInited || troopList == null) {
            if(!requestGroupList(service)) {
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
                    adminList = groupInfo.Administrator
                        .split("|", ",")
                        .map { it }
                        .apply { (this as ArrayList<String>).add(0, groupInfo.troopowneruin ?: "0") },
                    classText = groupInfo.mGroupClassExtText,
                    isFrozen = groupInfo.mIsFreezed != 0,
                    //troopLevel = groupInfo.newTroopLevelMap,
                    maxMember = groupInfo.wMemberMax,
                    memNum = groupInfo.wMemberNum
                ))
            }
        })
    }

    private suspend fun requestGroupList(service: ITroopInfoService): Boolean {
        TroopRequestHelper.refreshTroopList()
        return suspendCancellableCoroutine { continuation ->
            val waiter = GlobalScope.launch {
                do {
                    delay(1000)
                } while (!service.isTroopCacheInited)
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