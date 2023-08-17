package moe.fuqiuluo.http.action.helper

import com.tencent.qqnt.kernel.nativeinterface.MemberInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import kotlin.coroutines.resume

internal object TroopHelper {
    suspend fun getTroopMemberInfoByUin(groupId: String, qq: Long): MemberInfo? {
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