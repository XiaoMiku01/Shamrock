package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.IndexData
import mqq.app.MobileQQ

fun Routing.index() {
    get("/") {
        this.call.respond(CommonResult("ok", 0, IndexData(
            MobileQQ.getMobileQQ().qqProcessName, HTTPServer.startTime
        )))
    }
}