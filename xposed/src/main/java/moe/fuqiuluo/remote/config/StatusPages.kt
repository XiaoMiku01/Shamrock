package moe.fuqiuluo.remote.config

import moe.protocol.servlet.helper.ErrorTokenException
import moe.protocol.servlet.helper.LogicException
import moe.protocol.servlet.helper.ParamsException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import moe.fuqiuluo.remote.entries.CommonResult
import moe.fuqiuluo.remote.entries.ErrorCatch
import moe.fuqiuluo.remote.entries.Status

fun Application.statusPages() {
    install(StatusPages) {
        exception<ParamsException> { call, cause ->
            call.respond(CommonResult("failed", Status.BadParam.code, ErrorCatch(call.request.uri, cause.message ?: "")))
        }
        exception<LogicException> { call, cause ->
            call.respond(CommonResult("failed", Status.LogicError.code, ErrorCatch(call.request.uri, cause.message ?: "")))
        }
        exception<ErrorTokenException> { call, cause ->
            call.respond(CommonResult("failed", Status.BadRequest.code, ErrorCatch(call.request.uri, cause.message ?: "")))
        }
        exception<Throwable> { call, cause ->
            call.respond(CommonResult("failed", Status.InternalHandlerError.code, ErrorCatch(call.request.uri, cause.stackTraceToString())))
        }
    }
}