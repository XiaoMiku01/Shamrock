package moe.fuqiuluo.shamrock.ui.service.handlers

import android.content.ContentValues
import android.content.Context
import moe.fuqiuluo.shamrock.ui.app.AppRuntime

object LogHandler: ModuleHandler() {
    override val cmd: String = "send_message"

    override fun onReceive(callbackId: Int, values: ContentValues, context: Context) {
        AppRuntime.log(values.getAsString("string"))
    }
}