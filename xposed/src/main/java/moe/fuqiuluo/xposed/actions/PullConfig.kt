package moe.fuqiuluo.xposed.actions

import android.content.Context
import moe.protocol.service.helper.ShamrockConfig
import moe.fuqiuluo.xposed.helper.internal.DataRequester

import moe.fuqiuluo.remote.HTTPServer
import moe.protocol.servlet.utils.PlatformUtils
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.loader.ActionLoader
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.fuqiuluo.xposed.loader.NativeLoader
import mqq.app.MobileQQ
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class PullConfig: IAction {
    companion object {
        @JvmStatic
        var isConfigOk = false
    }

    private external fun testNativeLibrary(): String

    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMainProcess()) return

        DynamicReceiver.register("fetchPort", IPCRequest {
            DataRequester.request("success", mapOf(
                "port" to HTTPServer.currServerPort,
                "voice" to NativeLoader.isVoiceLoaded
            ))
        })
        DynamicReceiver.register("checkAndStartService", IPCRequest {
            if (HTTPServer.isQueryServiceStarted) {
                HTTPServer.isQueryServiceStarted = false
            }
            initHttp(MobileQQ.getContext())
        })
        DynamicReceiver.register("push_config", IPCRequest {
            ctx.toast("动态推送配置文件成功。")
            ShamrockConfig.updateConfig(ctx, it)
        })
        DynamicReceiver.register("change_port", IPCRequest {
            when (it.getStringExtra("type")) {
                "port" -> {
                    ctx.toast("动态修改HTTP端口成功。")
                    HTTPServer.changePort(it.getIntExtra("port", 5700))
                }
                "ws_port" -> {
                    ctx.toast("动态修改WS端口不支持。")
                    //WsServer.changePort(it.getIntExtra("port", 5800))
                }
            }
        })

        DataRequester.request("init", onFailure = {
            if (!ShamrockConfig.isInit(ctx)) {
                ctx.toast("请启动Shamrock主进程以初始化服务，进程将退出。")
                thread {
                    Thread.sleep(3000)
                    exitProcess(1)
                }
            } else {
                ctx.toast("Shamrock进程未启动，不会推送配置文件。")
                initHttp(ctx)
            }
        }, bodyBuilder = null) {
            isConfigOk = true
            ShamrockConfig.updateConfig(ctx, it)
            initHttp(ctx)
        }
    }

    private fun initHttp(ctx: Context) {
        NativeLoader.load("shamrock")
        ctx.toast(testNativeLibrary())
        ActionLoader.runService(ctx)
    }
}