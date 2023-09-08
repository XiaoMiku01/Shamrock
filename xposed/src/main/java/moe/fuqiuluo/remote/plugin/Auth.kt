package moe.fuqiuluo.remote.plugin

import moe.protocol.service.config.ShamrockConfig
import moe.protocol.servlet.helper.ErrorTokenException
import io.ktor.server.application.createApplicationPlugin

internal val Auth = createApplicationPlugin("Auth") {
    // 获取get请求的token参数并校验
    val token = ShamrockConfig.getToken()
    if (token.isBlank()) {
        return@createApplicationPlugin
    }
    this.onCallReceive { call, _ ->
        var accessToken = call.request.queryParameters["access_token"]
            ?: call.request.headers["Authorization"]
            ?: throw ErrorTokenException()
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7)
        }
        if (token != accessToken) {
            throw ErrorTokenException()
        }
    }
}