package moe.fuqiuluo.remote.api

import io.ktor.server.application.call
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Routing
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.getOrPost

fun Routing.showLog() {
    getOrPost("/log") {
        val log = LogCenter.getAllLog()
        call.respondFile(log)
    }
}