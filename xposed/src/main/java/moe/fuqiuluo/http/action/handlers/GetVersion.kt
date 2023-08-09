package moe.fuqiuluo.http.action.handlers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString

internal object GetVersion: IActionHandler() {
    override fun handle(session: ActionSession): String {
        return resultToString(true, Status.Ok, VersionInfo(
            "shamrock", "1.0.1", "12"
        ))
    }

    override fun path(): String = "get_version"

    @Serializable
    data class VersionInfo (
        val impl: String,
        val version: String,
        @SerialName("onebot_version")
        val onebotVersion: String
    )
}


