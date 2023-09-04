package moe.fuqiuluo.xposed.ipc

import android.os.IBinder
import moe.protocol.servlet.utils.PlatformUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import mqq.app.MobileQQ
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object ShamrockIpc {
    const val IPC_QSIGN = "qsign"

    private val IpcChannel = hashMapOf(
        IPC_QSIGN to QSignGenerator
    )
    private val mLock = Mutex()

    suspend fun get(name: String?): IBinder? {
        return if (PlatformUtils.isMsfProcess()) {
            IpcChannel[name]
        } else {
            mLock.withLock {
                MobileQQ.getContext().broadcast("msf") {
                    putExtra("__cmd", "fetch_ipc")
                    putExtra("ipc_name", name)
                }
                withTimeoutOrNull(3000) {
                    suspendCoroutine { continuation ->
                        DynamicReceiver.register("ipc_callback", IPCRequest {
                            val bundle = it.getBundleExtra("ipc")!!
                            val binder = bundle.getBinder("binder")
                            continuation.resume(binder)
                        })
                    }
                }
            }
        }
    }
}