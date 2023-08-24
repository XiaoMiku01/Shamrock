package moe.fuqiuluo.http.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.data.troop.TroopMemberInfo
import com.tencent.mobileqq.troop.api.ITroopMemberInfoService
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.SimpleTroopMemberInfo
import moe.fuqiuluo.http.action.helper.TroopRequestHelper
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty
import mqq.app.MobileQQ

internal object GetTroopMemberList: IActionHandler() {
    private val refreshLock = Mutex()

    override suspend fun handle(session: ActionSession): String {
        if (!session.has("group_id")) {
            return noParam("group_id")
        }
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)

        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val service = runtime.getRuntimeService(ITroopMemberInfoService::class.java, "all")
        var memberList = service.getAllTroopMembers(groupId)
        if (refresh || memberList == null) {
            memberList = requestTroopMemberInfo(service, groupId.toLong())
        }

        if (memberList == null) {
            return logic("obtain troop member info failed")
        }

        return ok(arrayListOf<SimpleTroopMemberInfo>().apply {
            memberList.forEach {  info ->
                if (info.memberuin != "0") {
                    add(
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
            }
        })
    }

    private suspend fun requestTroopMemberInfo(service: ITroopMemberInfoService, groupId: Long): List<TroopMemberInfo>? {
        return refreshLock.withLock {
            val groupIdStr = groupId.toString()
            service.deleteTroopMembers(groupIdStr)
            TroopRequestHelper.refreshTroopMemberList(groupId)

            withTimeoutOrNull(10000) {
                var memberList: List<TroopMemberInfo>?
                do {
                    delay(500)
                    memberList = service.getAllTroopMembers(groupIdStr)
                } while (memberList.isNullOrEmpty())
                return@withTimeoutOrNull memberList
            }
        }
    }

    override fun path(): String = "get_group_member_list"
}

