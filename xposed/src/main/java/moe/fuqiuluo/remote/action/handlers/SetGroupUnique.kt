package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SetGroupUnique: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        return ""
    }

    override fun path(): String = "set_group_special_title"
}