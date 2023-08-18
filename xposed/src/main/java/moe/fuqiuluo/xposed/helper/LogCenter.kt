package moe.fuqiuluo.xposed.helper

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import moe.fuqiuluo.http.action.helper.FileHelper
import moe.fuqiuluo.xposed.actions.impl.toast
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import mqq.app.MobileQQ
import java.util.Date

internal enum class Level(
    val id: Byte
) {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
}

internal object LogCenter {
    private val LogFile = FileHelper.getFile(
        dir = "log",
        name = MobileQQ.getMobileQQ().qqProcessName.replace(":", ".") + ".log"
    )
    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("[HH:mm:ss] ")

    fun log(
        string: String,
        level: Level = Level.INFO,
        toast: Boolean = false
    ) {
        if (toast) {
            MobileQQ.getContext().toast(string)
        }
        // 把日志广播到主进程
        DataRequester.request("send_message", bodyBuilder = {
            put("string", string)
            put("level", level.id)
        })

        if (!LogFile.exists()) {
            LogFile.createNewFile()
        }
        val format = "%s%s %s\n".format(format.format(Date()), level.name, string)

        LogFile.appendText(format)
    }

}