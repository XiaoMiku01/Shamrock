@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.remote.action.handlers

import com.tencent.mobileqq.friend.api.IFriendDataService
import com.tencent.mobileqq.friend.api.IFriendHandlerService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.FriendEntry
import com.tencent.mobileqq.data.PlatformType
import mqq.app.AppRuntime
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object GetFriendList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val refresh = session.getBooleanOrDefault("refresh", false)
        val service = runtime.getRuntimeService(IFriendDataService::class.java, "all")
        if(refresh || !service.isInitFinished) {
            if(!requestFriendList(runtime, service)) {
                return logic("get friendlist failed, please check your account or network.")
            }
        }
        return ok(service.allFriends.map { friend ->
            FriendEntry(
                id = friend.uin,
                name = friend.name,
                displayName = friend.remark,
                remark = friend.remark,
                age = friend.age,
                gender = friend.gender,
                groupId = friend.groupid,
                platformType = PlatformType.valueOf(friend.iTermType),
                termType = friend.iTermType
            )
        })
    }

    private suspend fun requestFriendList(runtime: AppRuntime, dataService: IFriendDataService): Boolean {
        val service = runtime.getRuntimeService(IFriendHandlerService::class.java, "all")
        service.requestFriendList(true, 0)
        return suspendCancellableCoroutine { continuation ->
            val waiter = GlobalScope.launch {
                while (!dataService.isInitFinished) {
                    delay(200)
                }
                continuation.resume(true)
            }
            continuation.invokeOnCancellation {
                waiter.cancel()
                continuation.resume(false)
            }
        }
    }



    override fun path(): String = "get_friend_list"
}