package moe.fuqiuluo.http.action

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.handlers.TestHandler
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asJsonArray
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString

internal object ActionManager {
    private val actionMap = mutableMapOf(
        "test" to TestHandler
    )

    operator fun get(action: String): ActionHandler<*>? {
        return actionMap[action]
    }
}

internal interface ActionHandler<T: Any> {
    fun handle(session: ActionSession): CommonResult<T?>
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