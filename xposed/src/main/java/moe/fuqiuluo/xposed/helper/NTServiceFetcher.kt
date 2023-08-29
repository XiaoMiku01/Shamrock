package moe.fuqiuluo.xposed.helper

import com.tencent.mobileqq.listener.AioListener
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.utils.PlatformUtils
import de.robv.android.xposed.XposedBridge
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.fuqiuluo.xposed.tools.hookMethod
import mqq.app.api.IRuntimeService

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var isRegisteredMSG = atomic(false)
    private var isForcedFore = atomic(false)
    private val lock = Mutex()

    suspend fun onFetch(service: IRuntimeService) {
        lock.withLock {
            if (service is IKernelService && !this::iKernelService.isInitialized) {
                this.iKernelService = service
                XposedBridge.log("Fetch kernel service successfully: $iKernelService")
            }
            if (this::iKernelService.isInitialized && !isRegisteredMSG.value) registerMSG()
            if (this::iKernelService.isInitialized && !isForcedFore.value) antiBackground()
        }
    }

    private fun registerMSG() {
        kotlin.runCatching {
            if (!PlatformUtils.isMainProcess()) return

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

    private fun antiBackground() {
        kotlin.runCatching {
            val kernelService = NTServiceFetcher.kernelService
            if (kernelService.isInit) {
                val sessionService = kernelService.wrapperSession

                sessionService.javaClass.hookMethod("switchToBackGround").before {
                    LogCenter.log("阻止进入后台模式！", Level.DEBUG)
                    it.result = null
                }

                val msgService = sessionService.msgService
                msgService.javaClass.hookMethod("switchBackGroundForMqq").before {
                    LogCenter.log("阻止进入后台模式！", Level.DEBUG)
                    val cb = it.args[1] as IOperateCallback
                    cb.onResult(-1, "injected")
                    it.result = null
                }
                msgService.javaClass.hookMethod("switchBackGround").before {
                    LogCenter.log("阻止进入后台模式！", Level.DEBUG)
                    val cb = it.args[1] as IOperateCallback
                    cb.onResult(-1, "injected")
                    it.result = null
                }
                LogCenter.log("反后台模式注入成功！", Level.DEBUG)
                isForcedFore.lazySet(true)
            }
        }.onFailure {
            LogCenter.log("Keeping NT alive failed: ${it.message}", Level.WARN)
        }
    }

    val kernelService: IKernelService
        get() = iKernelService
}