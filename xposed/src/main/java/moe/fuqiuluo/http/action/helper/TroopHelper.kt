package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.data.troop.TroopMemberInfo
import com.tencent.mobileqq.troop.api.ITroopMemberInfoService
import com.tencent.qqnt.kernel.nativeinterface.MemberInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.http.action.handlers.GetTroopMemberInfo
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object TroopHelper {
    private val RefreshTroopMemberInfoLock = Mutex()

    suspend fun getTroopMemberInfoByUin(
        groupId: String,
        uin: String,
        refresh: Boolean = false
    ): TroopMemberInfo? {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val service = runtime.getRuntimeService(ITroopMemberInfoService::class.java, "all")
        var info = service.getTroopMember(groupId, uin)
        if (refresh || !service.isMemberInCache(groupId, uin) || info == null || info.troopnick == null) {
            info = requestTroopMemberInfo(service, groupId.toLong(), uin.toLong())
        }

        return info
    }

    private suspend fun requestTroopMemberInfo(service: ITroopMemberInfoService, groupId: Long, memberUin: Long): TroopMemberInfo? {
        return RefreshTroopMemberInfoLock.withLock {
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

    suspend fun getTroopMemberInfoByUinViaNt(groupId: String, qq: Long): MemberInfo? {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        return suspendCancellableCoroutine {
            groupService.getTransferableMemberInfo(groupId.toLong()) { code, _, data ->
                if (code != 0) {
                    it.resume(null)
                    return@getTransferableMemberInfo
                }
                data.forEach { (_, info) ->
                    if (info.uin == qq) {
                        it.resume(info)
                        return@forEach
                    }
                }
                it.resume(null)
            }
        }
    }

    suspend fun getTroopMemberInfoByUid(groupId: String, uid: String): MemberInfo? {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        return suspendCancellableCoroutine {
            groupService.getTransferableMemberInfo(groupId.toLong()) { code, _, data ->
                if (code != 0) {
                    it.resume(null)
                    return@getTransferableMemberInfo
                }
                data.forEach { (tmpUid, info) ->
                    if (tmpUid == uid) {
                        it.resume(info)
                        return@forEach
                    }
                }
            }
        }
    }
}