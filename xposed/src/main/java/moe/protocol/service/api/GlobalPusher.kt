package moe.protocol.service.api

import moe.protocol.service.HttpService
import moe.protocol.service.WebSocketClientService
import moe.protocol.service.WebSocketService
import moe.protocol.service.config.ShamrockConfig

internal val GlobalPusher: Array<BasePushServlet>
    // isIgnoreAllEvent 忽略所有事件 不推送
    get() = if (ShamrockConfig.isIgnoreAllEvent()) arrayOf() else arrayOf(
        WebSocketService, HttpService, WebSocketClientService
    )


