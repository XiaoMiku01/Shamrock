package moe.fuqiuluo.shamrock.ui.app

import android.content.Context
import moe.fuqiuluo.shamrock.ui.service.internal.broadcastToModule

object ShamrockConfig {
    fun getHttpAddr(ctx: Context): String {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getString("http_addr", "shamrock.moe:80")!!
    }

    fun setHttpAddr(ctx: Context, v: String) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putString("http_addr", v).apply()
        pushUpdate(ctx)
    }

    fun isPro(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("pro_api", false)
    }

    fun setPro(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("pro_api", v).apply()
        pushUpdate(ctx)
    }

    fun getToken(ctx: Context): String {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getString("token", null) ?: ""
    }

    fun setToken(ctx: Context, v: String?) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putString("token", v).apply()
        pushUpdate(ctx)
    }

    fun isWs(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("ws", false)
    }

    fun setWs(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("ws", v).apply()
        pushUpdate(ctx)
    }

    fun isWsClient(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("ws_client", false)
    }

    fun setWsClient(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("ws_client", v).apply()
        pushUpdate(ctx)
    }

    fun isTablet(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("tablet", false)
    }

    fun setTablet(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("tablet", v).apply()
        pushUpdate(ctx)
    }

    fun isUseCQCode(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("use_cqcode", false)
    }

    fun setUseCQCode(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("use_cqcode", v).apply()
        pushUpdate(ctx)
    }

    fun isWebhook(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("webhook", false)
    }

    fun setWebhook(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("webhook", v).apply()
        pushUpdate(ctx)
    }

    fun getWsAddr(ctx: Context): String {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getString("ws_addr", "shamrock.moe:81")!!
    }

    fun setWsAddr(ctx: Context, v: String) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putString("ws_addr", v).apply()
        pushUpdate(ctx)
    }

    fun getHttpPort(ctx: Context): Int {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getInt("port", 5700)
    }

    fun setHttpPort(ctx: Context, v: Int) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putInt("port", v).apply()
        ctx.broadcastToModule {
            putExtra("type", "port")
            putExtra("port", v)
            putExtra("__cmd", "change_port")
        }
        pushUpdate(ctx)
    }

    fun getWsPort(ctx: Context): Int {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getInt("ws_port", 5800)
    }

    fun setWsPort(ctx: Context, v: Int) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putInt("ws_port", v).apply()
        ctx.broadcastToModule {
            putExtra("type", "ws_port")
            putExtra("port", v)
            putExtra("__cmd", "change_port")
        }
        pushUpdate(ctx)
    }

    fun is2B(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("2B", false)
    }

    fun set2B(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("2B", v).apply()
    }

    fun setAutoClean(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("auto_clear", v).apply()
    }

    fun isAutoClean(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("auto_clear", false)
    }

    fun isInjectPacket(ctx: Context): Boolean {
        val preferences = ctx.getSharedPreferences("config", 0)
        return preferences.getBoolean("inject_packet", false)
    }

    fun setInjectPacket(ctx: Context, v: Boolean) {
        val preferences = ctx.getSharedPreferences("config", 0)
        preferences.edit().putBoolean("inject_packet", v).apply()
    }

    fun getConfigMap(ctx: Context): Map<String, Any?> {
        val preferences = ctx.getSharedPreferences("config", 0)
        return mapOf(
            "tablet" to preferences.getBoolean("tablet", false),
            "port" to preferences.getInt("port", 5700),
            "ws" to preferences.getBoolean("ws", false),
            "ws_port" to preferences.getInt("ws_port", 5800),
            "http" to preferences.getBoolean("webhook", false),
            "http_addr" to preferences.getString("http_addr", ""),
            "ws_client" to preferences.getBoolean("ws_client", false),
            "use_cqcode" to preferences.getBoolean("use_cqcode", false),
            "ws_addr" to preferences.getString("ws_addr", ""),
            "pro_api" to preferences.getBoolean("pro_api", false),
            "token" to preferences.getString("token", null),
            "inject_packet" to preferences.getBoolean("inject_packet", false),
        )
    }

    fun pushUpdate(ctx: Context) {
        ctx.broadcastToModule {
            getConfigMap(ctx).forEach { (key, value) ->
                if (value == null) {
                    val v: String? = null
                    this.putExtra(key, v)
                } else {
                    when (value) {
                        is Int -> this.putExtra(key, value)
                        is Long -> this.putExtra(key, value)
                        is Short -> this.putExtra(key, value)
                        is Byte -> this.putExtra(key, value)
                        is String -> this.putExtra(key, value)
                        is ByteArray -> this.putExtra(key, value)
                        is Boolean -> this.putExtra(key, value)
                        is Float -> this.putExtra(key, value)
                        is Double -> this.putExtra(key, value)
                    }
                }
            }
            putExtra("__cmd", "push_config")
        }
    }
}