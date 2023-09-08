package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.servlet.CardSvc

internal object SetModelShow: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val model = session.getString("model")
        return invoke(model, session.echo)
    }

    suspend operator fun invoke(model: String, echo: String = ""): String {
        CardSvc.setModelShow(model)
        return ok("成功", echo = echo)
    }

    override val requiredParams: Array<String> = arrayOf("model")

    override fun path(): String = "_set_model_show"
}