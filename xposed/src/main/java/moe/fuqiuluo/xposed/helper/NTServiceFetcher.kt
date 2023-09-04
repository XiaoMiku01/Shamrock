package moe.fuqiuluo.xposed.helper

import moe.protocol.service.listener.AioListener
import moe.protocol.service.listener.GroupEventListener
import moe.protocol.service.listener.NetworkListener
import moe.protocol.service.listener.PrimitiveListener
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.IQQNTWrapperNetwork
import moe.protocol.servlet.utils.PlatformUtils
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.fuqiuluo.xposed.tools.hookMethod
import mqq.app.api.IRuntimeService

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var isInitedForNT = atomic(false)
    private var isForcedFore = atomic(false)
    private val lock = Mutex()

    suspend fun onFetch(service: IRuntimeService) {
        lock.withLock {
            if (service is IKernelService && !this::iKernelService.isInitialized) {
                this.iKernelService = service
                LogCenter.log("Fetch kernel service successfully: $iKernelService")
            }
            if (::iKernelService.isInitialized) {
                if (!isInitedForNT.value) {
                    initNTKernelListener()
                }
                if (!isForcedFore.value) {
                    antiBackgroundMode()
                }
                PrimitiveListener.listenTo()
            }
        }
    }

    private fun initNTKernelListener() {
        if (!PlatformUtils.isMainProcess()) return

        try {
            val kernelService = NTServiceFetcher.kernelService
            val sessionService = kernelService.wrapperSession

            val msgService = KernelServiceHelper.getMsgService(iKernelService) ?: return
            val groupService = sessionService.groupService ?: return
            //val networkService = IQQNTWrapperNetwork.CppProxy.openNetworkService() ?: return

            LogCenter.log("Register MSG listener successfully.")
            msgService.addMsgListener(AioListener)

            //networkService.addNetworkServiceListener(NetworkListener)
            //LogCenter.log("Register Network listener successfully.")

            groupService.addKernelGroupListener(GroupEventListener)
            LogCenter.log("Register Group listener successfully.")

            isInitedForNT.lazySet(true)
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.WARN)
        }
    }

    private fun antiBackgroundMode() {
        try {
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
        } catch (e: Throwable) {
            LogCenter.log("Keeping NT alive failed: ${e.message}", Level.WARN)
        }
    }

    val kernelService: IKernelService
        get() = iKernelService
}