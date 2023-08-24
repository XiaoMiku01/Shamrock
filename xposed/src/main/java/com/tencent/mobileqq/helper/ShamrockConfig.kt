package com.tencent.mobileqq.helper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import mqq.app.MobileQQ

internal object ShamrockConfig {
    private val preferences: SharedPreferences
        get() = MobileQQ.getContext().getSharedPreferences("shamrock_config", 0)

    fun isInit(context: Context): Boolean {
        val preferences = context.getSharedPreferences("shamrock_config", 0)
        return preferences.getBoolean("isInit", false)
    }

    fun updateConfig(context: Context, intent: Intent) {
        val preferences = context.getSharedPreferences("shamrock_config", 0)
        preferences.edit {
            putBoolean(  "tablet",     intent.getBooleanExtra("tablet", false))     // 强制平板模式
            putInt(      "port",       intent.getIntExtra("port", 5700))            // 主动HTTP端口
            putBoolean(  "ws",         intent.getBooleanExtra("ws", false))         // 主动WS开关
            putInt(      "ws_port",    intent.getIntExtra("port", 5700))            // 主动WS端口
            putBoolean(  "http",       intent.getBooleanExtra("http", false))       // HTTP回调开关
            putString(   "http_addr",  intent.getStringExtra("http_addr"))                     // WebHook回调地址
            putBoolean(  "ws_client",  intent.getBooleanExtra("ws_client", false))  // 被动WS开关
            putBoolean(  "use_cqcode", intent.getBooleanExtra("use_cqcode", false)) // 使用CQ码
            putString(   "ws_addr",    intent.getStringExtra("ws_addr"))                       // 被动WS地址
            putBoolean(  "pro_api",    intent.getBooleanExtra("pro_api", false))    // 开发调试API开关
            putBoolean("isInit", true)
        }
    }

    fun allowWebHook(): Boolean {
        return preferences.getBoolean("http", false)
    }

    fun getWebHookAddress(): String {
        return preferences.getString("http_addr", "") ?: ""
    }

    fun forceTablet(): Boolean {
        return preferences.getBoolean("tablet", true)
    }

    fun getPort(): Int {
        return preferences.getInt("port", 5700)
    }

    fun isPro(): Boolean {
        return preferences.getBoolean("pro_api", false)
    }
}