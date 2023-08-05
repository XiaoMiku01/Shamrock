package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond

fun Routing.getStartTime() {
    getOrPost("/get_start_time") {
        respond(
            isOk = true,
            code = Status.Ok,
            HTTPServer.startTime
        )
    }
}