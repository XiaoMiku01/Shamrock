@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions

import android.content.Context
import moe.protocol.service.helper.ShamrockConfig
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.remote.HTTPServer
import moe.fuqiuluo.remote.InternalWebSocketServer
import moe.fuqiuluo.remote.WebSocketServer
import moe.protocol.servlet.utils.PlatformUtils

internal class InitRemoteService: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMainProcess()) return

        GlobalScope.launch {
            try {
                HTTPServer.start(ShamrockConfig.getPort())
            } catch (e: Throwable) {
                XposedBridge.log(e)
            }
        }

        if (ShamrockConfig.openWebSocket()) {
            GlobalScope.launch {
                try {
                    if (InternalWebSocketServer != null) {
                        InternalWebSocketServer?.stop()
                    }
                    InternalWebSocketServer = WebSocketServer(ShamrockConfig.getWebSocketPort())
                    InternalWebSocketServer?.start()
                } catch (e: Throwable) {
                    XposedBridge.log(e)
                }
            }
        }
    }
}