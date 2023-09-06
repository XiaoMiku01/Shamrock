package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.entries.EmptyObject
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString

// 弱智玩意，不予实现
// 请开启HTTP回调 把事件回调回去
// 而不是在我这里轮询
internal object GetLatestEvents: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        return resultToString(
            true, Status.Ok, listOf<EmptyObject>(), echo = session.echo
        )
    }

    override fun path(): String = "get_latest_events"
}