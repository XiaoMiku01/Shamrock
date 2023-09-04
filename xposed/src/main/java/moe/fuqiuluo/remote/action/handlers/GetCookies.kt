package moe.fuqiuluo.remote.action.handlers

import moe.protocol.service.data.Credentials
import moe.protocol.servlet.protocol.TicketSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object GetCookies: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val domain = session.getStringOrNull("domain")
            ?: return invoke()
        return invoke(domain)
    }

    operator fun invoke(): String {
        val uin = TicketSvc.getUin()
        val skey = TicketSvc.getSKey(uin)
        val pskey = TicketSvc.getPSKey(uin)
        return ok(Credentials(cookie = "o_cookie=$uin; ied_qq=o$uin; pac_uid=1_$uin; uin=o$uin; skey=$skey; p_uin=o$uin; p_skey=$pskey;"))
    }

    operator fun invoke(domain: String): String {
        val uin = TicketSvc.getUin()
        val skey = TicketSvc.getSKey(uin)
        val pskey = TicketSvc.getPSKey(uin, domain) ?: ""
        val pt4token = TicketSvc.getPt4Token(uin, domain) ?: ""
        return ok(Credentials(cookie = "o_cookie=$uin; ied_qq=o$uin; pac_uid=1_$uin; uin=o$uin; skey=$skey; p_uin=o$uin; p_skey=$pskey; pt4_token=$pt4token;"))
    }

    override fun path(): String = "get_cookies"
}