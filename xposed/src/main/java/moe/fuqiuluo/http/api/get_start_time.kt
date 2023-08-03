package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.getStartTime() {
    getOrPost("/get_start_time") {
        call.respond(CommonResult("ok", 0, HTTPServer.startTime))
    }
}