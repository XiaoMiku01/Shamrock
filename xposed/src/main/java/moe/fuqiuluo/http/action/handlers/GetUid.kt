package moe.fuqiuluo.http.action.handlers

import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import moe.fuqiuluo.xposed.tools.asString
import kotlin.coroutines.resume

internal object GetUid: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
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