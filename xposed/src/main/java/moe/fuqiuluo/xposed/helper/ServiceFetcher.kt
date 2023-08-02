package moe.fuqiuluo.xposed.helper

import com.tencent.mobileqq.listener.AIOMSGListener
import com.tencent.qqnt.kernel.api.IKernelService
import de.robv.android.xposed.XposedBridge
import kotlinx.atomicfu.atomic
import mqq.app.api.IRuntimeService

object ServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var isRegisteredMSG = atomic(false)

    fun onFetch(service: IRuntimeService) {
        if (service is IKernelService && !this::iKernelService.isInitialized) {
            this.iKernelService = service
            XposedBridge.log("Fetch kernel service successfully: $iKernelService")
        }
        if (this::iKernelService.isInitialized && !isRegisteredMSG.value) registerMSG()
    }

    private fun registerMSG() {
        kotlin.runCatching {
            val msgService = KernelServiceHelper.getMsgService(iKernelService)
            if (msgService != null) {
                XposedBridge.log("Register MSG listener successfully.")

                msgService.addMsgListener(AIOMSGListener) // 注册消息监听器

                isRegisteredMSG.lazySet(true)
            }
        }.onFailure {
            XposedBridge.log(it)
        }
    }
}