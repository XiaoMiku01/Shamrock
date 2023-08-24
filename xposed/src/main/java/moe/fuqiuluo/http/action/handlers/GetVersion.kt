package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import com.tencent.mobileqq.data.VersionInfo
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString

internal object GetVersion: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        return resultToString(true, Status.Ok, VersionInfo(
            "shamrock", "1.0.1", "12"
        )
        )
    }

    override fun path(): String = "get_version"


}


