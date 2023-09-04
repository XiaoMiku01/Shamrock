package moe.fuqiuluo.xposed.helper

import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import mqq.app.MobileQQ

internal object PacketHandler {
    /*
    MSF 进程包处理是否就绪
     */
    var isInit = false

    init {
        DynamicReceiver.register("msf_waiter", IPCRequest {
            isInit = true
        })
    }

    // 注册一个包处理器
    fun register(cmd: String, callback: (ByteArray) -> Unit) {
        if (!isInit) return
        // 向MSF进程广播要求添加处理器
        MobileQQ.getContext().broadcast("msf") {
            putExtra("__cmd", "register_handler_cmd")
            putExtra("handler_cmd", cmd)
        }
        // 在本地广播接收器注册对应处理器
        DynamicReceiver.register(cmd, IPCRequest {
            val buffer = it.getByteArrayExtra("buffer")!!
            callback(buffer)
        })
    }

    fun unregister(cmd: String) {
        if (!isInit) return
        MobileQQ.getContext().broadcast("msf") {
            putExtra("__cmd", "unregister_handler_cmd")
            putExtra("handler_cmd", cmd)
        }
        DynamicReceiver.unregister(cmd)
    }
}