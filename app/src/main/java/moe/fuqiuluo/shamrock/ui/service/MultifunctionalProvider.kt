package moe.fuqiuluo.shamrock.ui.service

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import moe.fuqiuluo.shamrock.ui.app.AppRuntime

class MultifunctionalProvider: ContentProvider() {
    override fun insert(uri: Uri, content: ContentValues?): Uri {
        requireNotNull(content)
        requireNotNull(context)

        val hash = content.getAsInteger("hash")
        when(content.getAsString("cmd")) {
            "init" -> {
                AppRuntime.log("推送QQ进程初始化设置数据包成功...")
                val preferences = context!!.getSharedPreferences("config", 0)
                broadcast {
                    putExtra("tablet", preferences.getBoolean("tablet", false)) // 强制平板模式
                    putExtra("port", preferences.getInt("port", 5700)) // 主动HTTP端口
                    putExtra("ws", preferences.getBoolean("ws", false)) // 主动WS开关
                    putExtra("ws_port", preferences.getInt("ws_port", 5800)) // 主动WS端口
                    putExtra("http", preferences.getBoolean("webhook", false)) // HTTP回调开关
                    putExtra("http_addr", preferences.getString("http_addr", "")) // WebHook回调地址
                    putExtra("ws_client", preferences.getBoolean("ws_client", false)) // 被动WS开关
                    putExtra("ws_addr", preferences.getString("ws_addr", "")) // 被动WS地址
                    putExtra("pro_api", preferences.getBoolean("pro_api", false)) // 开发调试API开关
                    putExtra("hash", hash)
                }
            }
            "success" -> {
                DashboardInitializer(content.getAsInteger("port"))
                broadcast { putExtra("hash", hash) }
            }
        }


        return uri
    }

    private fun broadcast(intentBuilder: Intent.() -> Unit) {
        val intent = Intent()
        intent.action = "moe.fuqiuluo.xqbot.dynamic"
        intent.intentBuilder()
        context!!.sendBroadcast(intent)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        return null
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }
}