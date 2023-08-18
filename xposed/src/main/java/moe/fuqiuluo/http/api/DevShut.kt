package moe.fuqiuluo.http.api

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.delay
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.xposed.helper.LogCenter
import kotlin.system.exitProcess

fun Routing.shut() {
    get("/shut") {
        HTTPServer.stop()
        LogCenter.log("正在关闭Shamrock。", toast = true)
        delay(3000)
        exitProcess(0)
    }
}