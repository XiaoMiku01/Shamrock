package moe.fuqiuluo.xposed.tools

import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext

fun PipelineContext<Unit, ApplicationCall>.fetchGet(key: String): String {
    return call.parameters[key]!!
}

fun PipelineContext<Unit, ApplicationCall>.fetchGetOrNull(key: String): String? {
    return call.parameters[key]
}

fun Parameters.fetchPost(key: String): String {
    return this[key]!!
}

fun Parameters.fetchPostOrNull(key: String): String? {
    return this[key]
}

@io.ktor.util.KtorDsl
fun Routing.getOrPost(path: String, body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) {
    get(path, body)
    post(path, body)
}