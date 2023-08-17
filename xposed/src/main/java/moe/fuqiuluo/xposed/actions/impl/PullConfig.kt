package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import androidx.core.content.edit
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.internal.DataRequester

import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.loader.ActionLoader
import moe.fuqiuluo.xposed.loader.NativeLoader
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class PullConfig: IAction {
    companion object {
        @JvmStatic
        var isConfigOk = false
    }

    private external fun testNativeLibrary(): String

    override fun invoke(ctx: Context) {
        if (!PlatformHelper.isMainProcess()) return

        DynamicReceiver.register("fetchPort", IPCRequest {
            DataRequester.request("success", mapOf("port" to HTTPServer.PORT))
        })

        val preferences = ctx.getSharedPreferences("shamrock_config", 0)

        DataRequester.request("init", onFailure = {
            if (!preferences.getBoolean("isInit", false)) {
                ctx.toast("请启动Shamrock主进程以初始化服务，进程将退出。")
                thread {
                    Thread.sleep(3000)
                    exitProcess(1)
                }
            } else {
                ctx.toast("Shamrock进程未启动，不会推送配置文件。。")
            }
        }, bodyBuilder = null) {
            isConfigOk = true
            preferences.edit {
                putBoolean(  "tablet",    it.getBooleanExtra("tablet", false)) // 强制平板模式
                putInt(      "port",      it.getIntExtra("port", 5700)) // 主动HTTP端口
                putBoolean(  "ws",        it.getBooleanExtra("ws", false)) // 主动WS开关
                putInt(      "ws_port",   it.getIntExtra("port", 5700)) // 主动WS端口
                putBoolean(  "http",      it.getBooleanExtra("http", false)) // HTTP回调开关
                putString(   "http_addr", it.getStringExtra("http_addr")) // WebHook回调地址
                putBoolean(  "ws_client", it.getBooleanExtra("ws_client", false)) // 被动WS开关
                putString(   "ws_addr",   it.getStringExtra("ws_addr")) // 被动WS地址
                putBoolean(  "pro_api",   it.getBooleanExtra("pro_api", false)) // 开发调试API开关
                putBoolean("isInit", true)
            }

            NativeLoader.load("shamrock")
            ctx.toast(testNativeLibrary())

            ActionLoader.runService(ctx)
        }
    }
}