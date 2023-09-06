package moe.protocol.service.helper

import android.content.Context
import android.content.Intent
import moe.fuqiuluo.utils.MMKVFetcher

internal object ShamrockConfig {
    fun isInit(context: Context): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("isInit", false)
    }

    fun updateConfig(context: Context, intent: Intent) {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        mmkv.apply {
            putBoolean(  "tablet",     intent.getBooleanExtra("tablet", false))                 // 强制平板模式
            putInt(      "port",       intent.getIntExtra("port", 5700))                         // 主动HTTP端口
            putBoolean(  "ws",         intent.getBooleanExtra("ws", false))                     // 主动WS开关
            putInt(      "ws_port",    intent.getIntExtra("port", 5800))                         // 主动WS端口
            putBoolean(  "http",       intent.getBooleanExtra("http", false))                   // HTTP回调开关
            putString(   "http_addr",  intent.getStringExtra("http_addr"))                                  // WebHook回调地址
            putBoolean(  "ws_client",  intent.getBooleanExtra("ws_client", false))              // 被动WS开关
            putBoolean(  "use_cqcode", intent.getBooleanExtra("use_cqcode", false))             // 使用CQ码
            putString(   "ws_addr",    intent.getStringExtra("ws_addr"))                                    // 被动WS地址
            putBoolean(  "pro_api",    intent.getBooleanExtra("pro_api", false))                // 开发调试API开关
            putBoolean(  "inject_packet",    intent.getBooleanExtra("inject_packet", false))    // 拦截无用包
            putString(   "token",      intent.getStringExtra("token"))                                      // 鉴权
            putBoolean("isInit", true)
        }
    }

    fun openWebSocket(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("ws", false)
    }

    fun getWebSocketPort(): Int {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getInt("ws_port", 5800)
    }

    fun getToken(): String {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getString("token", "") ?: ""
    }

    fun useCQ(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("use_cqcode", false)
    }

    fun allowWebHook(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("http", false)
    }

    fun getWebHookAddress(): String {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getString("http_addr", "") ?: ""
    }

    fun forceTablet(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("tablet", true)
    }

    fun getPort(): Int {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getInt("port", 5700)
    }

    fun isPro(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("pro_api", false)
    }

    fun isInjectPacket(): Boolean {
        val mmkv = MMKVFetcher.mmkvWithId("shamrock_config")
        return mmkv.getBoolean("inject_packet", false)
    }
}