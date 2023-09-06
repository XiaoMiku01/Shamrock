package moe.fuqiuluo.remote.action.handlers

import kotlinx.serialization.Serializable
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString
import de.robv.android.xposed.XposedBridge.log

internal object TestHandler: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        kotlin.runCatching {
            val msg = StringBuffer()
            return resultToString(
                isOk = true,
                code = Status.Ok,
                data = Test(System.currentTimeMillis()),
                msg = msg.toString(),
                echo = session.echo
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