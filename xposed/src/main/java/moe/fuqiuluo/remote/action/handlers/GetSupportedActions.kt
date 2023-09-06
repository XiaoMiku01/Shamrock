package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.action.ActionManager
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.remote.entries.resultToString

internal object GetSupportedActions: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        return resultToString(true, Status.Ok, ActionManager.actionMap.keys.toList(), echo = session.echo)
    }

    override fun path(): String = "get_supported_actions"
}