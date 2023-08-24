package moe.fuqiuluo.http.api

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.request.httpVersion
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import moe.fuqiuluo.http.HTTPServer
import moe.fuqiuluo.http.action.ActionManager
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.EmptyObject
import moe.fuqiuluo.http.entries.IndexData
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.respond
import mqq.app.MobileQQ

@Serializable
data class OldApiResult<T>(
    val code: Int,
    val msg: String = "",
    @Contextual
    val data: T? = null
)

fun Routing.index() {
    get("/") {
        respond(
            isOk = true,
            code = Status.Ok,
            data = IndexData(MobileQQ.getMobileQQ().qqProcessName, HTTPServer.startTime, call.request.httpVersion)
        )
    }

    // Action局
    post("/") {
        val jsonText = call.receiveText()
        val actionObject = Json.parseToJsonElement(jsonText).jsonObject

        val action = actionObject["action"].asString
        val params = actionObject["params"].asJsonObject

        val handler = ActionManager[action]
        if (handler == null) {
            respond(false, Status.UnsupportedAction, EmptyObject, "不支持的Action")
        } else {
            call.respondText(
                handler.handle(ActionSession(params)), ContentType.Application.Json
            )
        }
    }
}