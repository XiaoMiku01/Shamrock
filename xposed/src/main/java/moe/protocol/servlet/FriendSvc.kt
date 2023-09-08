package moe.protocol.servlet

import com.tencent.mobileqq.data.Friends
import com.tencent.mobileqq.friend.api.IFriendDataService
import com.tencent.mobileqq.friend.api.IFriendHandlerService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mqq.app.AppRuntime
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object FriendSvc: BaseSvc() {

    suspend fun getFriendList(refresh: Boolean): List<Friends>? {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val service = runtime.getRuntimeService(IFriendDataService::class.java, "all")
        if(refresh || !service.isInitFinished) {
            if(!requestFriendList(runtime, service)) {
                return null
            }
        }
        return service.allFriends
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
}