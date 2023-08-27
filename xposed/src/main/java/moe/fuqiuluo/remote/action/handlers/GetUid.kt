package moe.fuqiuluo.remote.action.handlers

import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import moe.fuqiuluo.xposed.tools.asString
import kotlin.coroutines.resume

internal object GetUid: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val uinList = session.getArray("uin_list").map {
            it.asString.toLong()
        }

        val uidMap = suspendCancellableCoroutine { continuation ->
            sessionService.uixConvertService.getUid(uinList.toHashSet()) {
                continuation.resume(it)
            }
        }
        return resultToString(true, Status.Ok, uidMap)
    }

    override fun path(): String = "get_uid"


}