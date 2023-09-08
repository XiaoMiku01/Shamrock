package moe.fuqiuluo.remote.action.handlers

import moe.protocol.service.data.Credentials
import moe.protocol.servlet.TicketSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object GetCookies: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val domain = session.getStringOrNull("domain")
            ?: return invoke(session.echo)
        return invoke(domain, session.echo)
    }

    operator fun invoke(echo: String = ""): String {
        return ok(Credentials(cookie = TicketSvc.getCookie()), echo)
    }

    operator fun invoke(domain: String, echo: String = ""): String {
        return ok(Credentials(cookie = TicketSvc.getCookie(domain)), echo)
    }

    override fun path(): String = "get_cookies"
}