package moe.fuqiuluo.remote.plugin

import com.tencent.mobileqq.helper.ShamrockConfig
import com.tencent.qqnt.msg.ErrorTokenException
import io.ktor.server.application.createApplicationPlugin

internal val Auth = createApplicationPlugin("Auth") {
    // 获取get请求的token参数并校验
    val token = ShamrockConfig.getToken()
    if (token.isBlank()) {
        return@createApplicationPlugin
    }
    this.onCallReceive { call, _ ->
        var accessToken = call.request.queryParameters["access_token"]
            ?: call.request.headers["Authorization"] ?: return@onCallReceive
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7)
        }
        if (token != accessToken) {
            throw ErrorTokenException()
        }
    }
}