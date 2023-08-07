package moe.fuqiuluo.http.action

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.handlers.*
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asJsonArray
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString

internal object ActionManager {
    val actionMap = mutableMapOf(
        "test" to TestHandler,
        "get_latest_events" to GetLatestEvents,
        "get_supported_actions" to GetSupportedActions,
        "get_status" to GetStatus,
        "get_version" to GetVersion,
        "get_self_info" to GetSelfInfo,

    )

    operator fun get(action: String): IActionHandler? {
        return actionMap[action]
    }
}

internal interface IActionHandler {
    fun handle(session: ActionSession): String

    fun path(): String
}

internal class ActionSession(
    private val params: JsonObject
) {
    fun getInt(key: String): Int {
        return params[key].asInt
    }

    fun getString(key: String): String {
        return params[key].asString
    }

    fun getObject(key: String): JsonObject {
        return params[key].asJsonObject
    }

    fun getArray(key: String): JsonArray {
        return params[key].asJsonArray
    }

    fun has(key: String) = params.containsKey(key)
}