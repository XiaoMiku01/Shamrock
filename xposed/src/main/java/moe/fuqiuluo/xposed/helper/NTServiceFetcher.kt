package moe.fuqiuluo.xposed.helper

import com.tencent.mobileqq.listener.AioListener
import com.tencent.qqnt.kernel.api.IKernelService
import de.robv.android.xposed.XposedBridge
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mqq.app.api.IRuntimeService

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var isRegisteredMSG = atomic(false)
    private val lock = Mutex()

    suspend fun onFetch(service: IRuntimeService) {
        lock.withLock {
            if (service is IKernelService && !this::iKernelService.isInitialized) {
                this.iKernelService = service
                XposedBridge.log("Fetch kernel service successfully: $iKernelService")
            }
            if (this::iKernelService.isInitialized && !isRegisteredMSG.value) registerMSG()
        }
    }

    private fun registerMSG() {
        kotlin.runCatching {
            val msgService = KernelServiceHelper.getMsgService(iKernelService)
            if (msgService != null) {
                XposedBridge.log("Register MSG listener successfully.")

                msgService.addMsgListener(AioListener) // 注册消息监听器

                isRegisteredMSG.lazySet(true)
            }
        }.onFailure {
            XposedBridge.log(it)
        }
    }

    val kernelService: IKernelService
        get() = iKernelService
}