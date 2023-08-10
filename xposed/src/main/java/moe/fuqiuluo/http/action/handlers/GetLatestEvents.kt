package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.EmptyObject
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString

// 弱智玩意，不予实现
// 请开启HTTP回调 把事件回调回去
// 而不是在我这里轮询
internal object GetLatestEvents: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        return resultToString(
            true, Status.Ok, listOf<EmptyObject>()
        )
    }

    override fun path(): String = "get_latest_events"
}