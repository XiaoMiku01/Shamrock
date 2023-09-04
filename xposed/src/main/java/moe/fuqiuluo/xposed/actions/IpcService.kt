package moe.fuqiuluo.xposed.actions

import android.content.Context
import android.os.Bundle
import moe.protocol.servlet.utils.PlatformUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.ipc.ShamrockIpc
import moe.fuqiuluo.xposed.tools.broadcast

internal class IpcService: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMsfProcess()) return
        DynamicReceiver.register("fetch_ipc", IPCRequest {
            val name = it.getStringExtra("ipc_name")
            GlobalScope.launch {
                ShamrockIpc.get(name)?.also { binder ->
                    ctx.broadcast("xqbot") {
                        putExtra("__cmd", "ipc_callback")
                        putExtra("ipc", Bundle().also {
                            it.putString("name", name)
                            it.putBinder("binder", binder)
                        })
                    }
                } ?: LogCenter.log("无法获取IPC: $name", Level.WARN)
            }
        })
    }
}
