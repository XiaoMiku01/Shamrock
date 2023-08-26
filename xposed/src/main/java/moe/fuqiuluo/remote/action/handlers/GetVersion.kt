package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.action.ActionSession
import com.tencent.mobileqq.data.VersionInfo
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString

internal object GetVersion: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        return resultToString(true, Status.Ok, VersionInfo(
            "shamrock", "1.0.1", "12"
        )
        )
    }

    override fun path(): String = "get_version"


}


