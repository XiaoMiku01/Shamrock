package moe.fuqiuluo.xposed.tools

import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import com.tencent.qqnt.msg.ParamsException
import io.ktor.http.HttpMethod
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.route
import moe.fuqiuluo.remote.entries.CommonResult
import moe.fuqiuluo.remote.entries.EmptyObject
import moe.fuqiuluo.remote.entries.Status

@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ShamrockDsl

suspend fun PipelineContext<Unit, ApplicationCall>.fetch(key: String): String {
    val isPost = call.request.httpMethod == HttpMethod.Post
    return if (isPost) {
        fetchPost(key)
    } else {
        fetchGet(key)
    }
}
suspend fun PipelineContext<Unit, ApplicationCall>.fetchOrNull(key: String): String? {
    val isPost = call.request.httpMethod == HttpMethod.Post
    return if (isPost) {
        fetchPostOrNull(key)
    } else {
        fetchGetOrNull(key)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchOrThrow(key: String): String {
    val isPost = call.request.httpMethod == HttpMethod.Post
    return if (isPost) {
        fetchPostOrThrow(key)
    } else {
        fetchGetOrThrow(key)
    }
}

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

fun PipelineContext<Unit, ApplicationCall>.isJsonData(): Boolean {
    return ContentType.Application.Json == call.request.contentType()
}

suspend fun PipelineContext<Unit, ApplicationCall>.isString(key: String): Boolean {
    if (!isJsonData()) return true
    val cacheKey = AttributeKey<JsonObject>("paramsJson")
    val data = if (call.attributes.contains(cacheKey)) {
        call.attributes[cacheKey]
    } else {
        Json.parseToJsonElement(call.receiveText()).jsonObject.also {
            call.attributes.put(cacheKey, it)
        }
    }
    return data[key] is JsonPrimitive
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchPostJsonObject(key: String): JsonObject {
    val cacheKey = AttributeKey<JsonObject>("paramsJson")
    val data = if (call.attributes.contains(cacheKey)) {
        call.attributes[cacheKey]
    } else {
        Json.parseToJsonElement(call.receiveText()).jsonObject.also {
            call.attributes.put(cacheKey, it)
        }
    }
    return data[key].asJsonObject
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchPostJsonArray(key: String): JsonArray {
    val cacheKey = AttributeKey<JsonObject>("paramsJson")
    val data = if (call.attributes.contains(cacheKey)) {
        call.attributes[cacheKey]
    } else {
        Json.parseToJsonElement(call.receiveText()).jsonObject.also {
            call.attributes.put(cacheKey, it)
        }
    }
    return data[key].asJsonArray
}

suspend fun PipelineContext<Unit, ApplicationCall>.fetchPostOrNull(key: String): String? {
    if (isJsonData()) {
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
    route(path) {
        get(body)
        post(body)
    }
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