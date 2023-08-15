package moe.fuqiuluo.http.action.helper

import com.tencent.qqnt.kernel.nativeinterface.MemberInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.xposed.helper.ServiceFetcher
import kotlin.coroutines.resume

internal object TroopHelper {
    suspend fun getTroopMemberInfoByUin(qq: Long): MemberInfo? {
        val kernelService = ServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        return suspendCancellableCoroutine {
            groupService.getTransferableMemberInfo(702991373) { code, _, data ->
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

    suspend fun getTroopMemberInfoByUid(uid: String): MemberInfo? {
        val kernelService = ServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        return suspendCancellableCoroutine {
            groupService.getTransferableMemberInfo(702991373) { code, _, data ->
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