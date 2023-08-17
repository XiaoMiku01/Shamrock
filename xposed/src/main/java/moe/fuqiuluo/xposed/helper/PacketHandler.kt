package moe.fuqiuluo.xposed.helper

import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.ICallback
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import mqq.app.MobileQQ
internal object PacketHandler {
    // 注册一个包处理器
    fun register(cmd: String, callback: ICallback) {
        // 向MSF进程广播要求添加处理器
        MobileQQ.getContext().broadcast("msf") {
            putExtra("__cmd", "register_handler_cmd")
            putExtra("handler_cmd", cmd)
        }
        // 在本地广播接收器注册对应处理器
        DynamicReceiver.register(cmd, IPCRequest(callback = callback))
    }

    fun unregister(cmd: String) {
        MobileQQ.getContext().broadcast("msf") {
            putExtra("__cmd", "unregister_handler_cmd")
            putExtra("handler_cmd", cmd)
        }
        DynamicReceiver.unregister(cmd)
    }
}