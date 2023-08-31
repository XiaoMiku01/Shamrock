package moe.fuqiuluo.remote.api

import com.tencent.qqnt.protocol.TicketSvc
import io.ktor.server.routing.Routing
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond

fun Routing.ticketActions() {
    getOrPost("/get_cookies") {

    }

    getOrPost("/get_ticket") {
        val uin = fetchOrThrow("uin")
        val ticket = when(fetchOrThrow("id").toInt()) {
            32 -> TicketSvc.getStWeb(uin)
            else -> error("不支持获取该Ticket")
        }
        respond(true, Status.Ok, "success", ticket)
    }
}