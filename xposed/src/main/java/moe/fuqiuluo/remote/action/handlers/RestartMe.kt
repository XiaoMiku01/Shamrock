package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object RestartMe: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        return invoke(2000)
    }

    operator fun invoke(duration: Int): String {
        return ok("成功")
    }

    override fun path(): String = "set_restart"
}