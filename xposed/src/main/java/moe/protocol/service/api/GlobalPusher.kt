package moe.protocol.service.api

import moe.protocol.service.HttpService
import moe.protocol.service.WebSocketService

internal val GlobalPusher = arrayOf(
    WebSocketService, HttpService
)


