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
import moe.fuqiuluo.http.action.data.SimpleTroopMemberInfo
import moe.fuqiuluo.http.action.helper.TroopRequestHelper
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty
import mqq.app.MobileQQ

internal object GetTroopMemberInfo: IActionHandler() {
    private val refreshLock = Mutex()

    override suspend fun handle(session: ActionSession): String {
        if (!session.has("user_id")) {
            return noParam("user_id")
        }
        val uin = session.getString("user_id")
        if (!session.has("group_id")) {
            return noParam("group_id")
        }
        val groupId = session.getString("group_id")
        val refresh = session.getBooleanOrDefault("refresh", false)

        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val service = runtime.getRuntimeService(ITroopMemberInfoService::class.java, "all")
        var info = service.getTroopMember(groupId, uin)
        if (refresh || !service.isMemberInCache(groupId, uin) || info == null || info.troopnick == null) {
            info = requestTroopMemberInfo(service, groupId.toLong(), uin.toLong())
        }

        if (info == null) {
            return logic("cannot get troop member info")
        }
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

    private suspend fun requestTroopMemberInfo(service: ITroopMemberInfoService, groupId: Long, memberUin: Long): TroopMemberInfo? {
        return refreshLock.withLock {
            val groupIdStr = groupId.toString()
            val memberUinStr = memberUin.toString()

            service.deleteTroopMember(groupIdStr, memberUinStr)

            TroopRequestHelper.requestMemberInfoV2(groupId, memberUin)
            TroopRequestHelper.requestMemberInfo(groupId, memberUin)

            withTimeoutOrNull(10000) {
                while (!service.isMemberInCache(groupIdStr, memberUinStr)) {
                    delay(200)
                }
                return@withTimeoutOrNull service.getTroopMember(groupIdStr, memberUinStr)
            }
        }
    }

    override fun path(): String = "get_group_member_info"
}

