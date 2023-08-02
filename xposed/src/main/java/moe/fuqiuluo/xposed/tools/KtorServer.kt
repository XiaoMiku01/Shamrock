package moe.fuqiuluo.xposed.tools

import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
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