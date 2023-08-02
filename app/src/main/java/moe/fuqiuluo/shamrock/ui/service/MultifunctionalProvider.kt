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
                    putExtra("hash", hash)
                }

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