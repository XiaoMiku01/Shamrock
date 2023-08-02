package moe.fuqiuluo.http.api

import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.atomicfu.atomic

var isStartedHTTPService = atomic(false)

fun Routing.startHTTPService() {
    get("/start") {

    }
}