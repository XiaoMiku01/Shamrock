package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler

internal object DeleteMessage: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {

        return "error"
    }
    override fun path(): String = "delete_message"
}