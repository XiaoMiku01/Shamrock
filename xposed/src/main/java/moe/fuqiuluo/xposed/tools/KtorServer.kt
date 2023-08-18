package moe.fuqiuluo.xposed.tools

import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.contentType
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import moe.fuqiuluo.http.action.helper.msg.ParamsException
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.EmptyObject
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

fun PipelineContext<Unit, ApplicationCall>.fetchGetOrThrow(key: String): String {
    return call.parameters[key] ?: throw ParamsException(key)
}


suspend fun PipelineContext<Unit, ApplicationCall>.fetchPost(key: String): String {
    return fetchPostOrNull(key)!!
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchPostOrThrow(key: String): String {
    return fetchPostOrNull(key) ?: throw ParamsException(key)
}


suspend fun PipelineContext<Unit, ApplicationCall>.fetchPostOrNull(key: String): String? {
    val contentType = call.request.contentType()
    if (ContentType.Application.Json == contentType) {
        val cacheKey = AttributeKey<JsonObject>("paramsJson")
        val data = if (call.attributes.contains(cacheKey)) {
            call.attributes[cacheKey]
        } else {
            Json.parseToJsonElement(call.receiveText()).jsonObject.also {
                call.attributes.put(cacheKey, it)
            }
        }
        return data[key].asStringOrNull
    } else {
        val cacheKey = AttributeKey<Parameters>("paramsParts")
        val data = if (call.attributes.contains(cacheKey)) {
            call.attributes[cacheKey]
        } else {
            call.receiveParameters().also {
                call.attributes.put(cacheKey, it)
            }
        }
        return data[key]
    }
}

@io.ktor.util.KtorDsl
fun Routing.getOrPost(path: String, body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit) {
    get(path, body)
    post(path, body)
}

@ShamrockDsl
suspend inline fun PipelineContext<Unit, ApplicationCall>.respond(
    isOk: Boolean,
    code: Status,
    msg: String = "",
    echo: String = ""
) {
    call.respond(CommonResult(
        if (isOk) "ok" else "failed",
        code.code,
        EmptyObject,
        msg,
        echo
    ))
}

@ShamrockDsl
suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.respond(
    isOk: Boolean,
    code: Status,
    data: T,
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
    data: T,
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