package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString

internal object GetSupportedActions: IActionHandler {
    override fun handle(session: ActionSession): String {
        return resultToString(true, Status.Ok, ActionManager.actionMap.keys.toList())
    }

    override fun path(): String = "get_supported_actions"
}