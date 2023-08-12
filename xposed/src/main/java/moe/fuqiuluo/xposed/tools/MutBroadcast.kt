package moe.fuqiuluo.xposed.tools

import android.content.Context
import android.content.Intent

internal fun Context.broadcast(processName: String, intentBuilder: Intent.() -> Unit) {
    val intent = Intent()
    intent.action = "moe.fuqiuluo.$processName.dynamic"
    intent.intentBuilder()
    sendBroadcast(intent)
}