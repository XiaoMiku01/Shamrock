package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object GetCookies: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {

        return "开始"
    }

    override fun path(): String = "get_cookies"
}