package moe.fuqiuluo.http.action.handlers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import mqq.app.MobileQQ


internal object GetStatus: IActionHandler() {
    override fun handle(session: ActionSession): String {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val curUin = runtime.currentAccountUin
        return resultToString(true, Status.Ok, listOf(
            BotStatus(
                Self("qq", curUin), runtime.isLogin, "正常"
            )
        ))
    }

    override fun path(): String = "get_status"

    @Serializable
    data class BotStatus(
        val self: Self,
        val online: Boolean,
        @SerialName("qq.status")
        val status: String
    )

    @Serializable
    data class Self(
        val platform: String,
        @SerialName("user_id")
        val userId: String
    )
}