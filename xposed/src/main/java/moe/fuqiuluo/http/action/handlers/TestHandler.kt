package moe.fuqiuluo.http.action.handlers

import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import de.robv.android.xposed.XposedBridge.log

internal object TestHandler: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        kotlin.runCatching {
            val msg = StringBuffer()


            return resultToString(
                isOk = true,
                code = Status.Ok,
                data = Test(System.currentTimeMillis()),
                msg = msg.toString()
            )
        }.onFailure {
            log(it)
        }
        return "error"
    }
    override fun path(): String = "test"
    @Serializable
    data class Test(val time: Long)
}