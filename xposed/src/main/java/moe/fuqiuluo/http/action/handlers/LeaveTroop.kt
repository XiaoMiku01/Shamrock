package moe.fuqiuluo.http.action.handlers

import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler

internal object LeaveTroop: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        TODO("Not yet implemented")
    }

    override fun path(): String {
        TODO("Not yet implemented")
    }

}