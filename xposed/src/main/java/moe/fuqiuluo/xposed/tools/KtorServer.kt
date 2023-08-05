package moe.fuqiuluo.xposed.tools

import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.reflect.TypeInfo
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.Status

@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ShamrockDsl

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

@ShamrockDsl
suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.respond(
    isOk: Boolean,
    code: Status,
    data: T? = null,
    msg: String = "",
    echo: String = ""
) {
    call.respond(CommonResult(
        if (isOk) "ok" else "failed",
        code.code,
        data,
        msg,
        echo
    ))
}

suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.respond(
    isOk: Boolean,
    code: Int,
    data: T? = null,
    msg: String = "",
    echo: String = ""
) {
    call.respond(CommonResult(
        if (isOk) "ok" else "failed",
        code,
        data,
        msg,
        echo
    ))
}