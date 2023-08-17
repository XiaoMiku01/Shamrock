package moe.fuqiuluo.shamrock.ui.service.handlers

import android.content.ContentValues
import android.content.Context
import moe.fuqiuluo.shamrock.ui.app.AppRuntime

internal object InitHandler: ModuleHandler() {
    override val cmd: String = "init"

    override fun onReceive(callbackId: Int, values: ContentValues, context: Context) {
        AppRuntime.log("推送QQ进程初始化设置数据包成功...")
        val preferences = context.getSharedPreferences("config", 0)
        callback(context, callbackId, mapOf(
            "tablet" to preferences.getBoolean("tablet", false),
            "port" to preferences.getInt("port", 5700),
            "ws" to preferences.getBoolean("ws", false),
            "ws_port" to preferences.getInt("ws_port", 5800),
            "http" to preferences.getBoolean("webhook", false),
            "http_addr" to preferences.getString("http_addr", ""),
            "ws_client" to preferences.getBoolean("ws_client", false),
            "ws_addr" to preferences.getString("ws_addr", ""),
            "pro_api" to preferences.getBoolean("pro_api", false)
        ))
    }
}