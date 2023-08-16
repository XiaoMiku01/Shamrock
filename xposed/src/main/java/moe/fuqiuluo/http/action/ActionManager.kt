package moe.fuqiuluo.http.action

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.handlers.*
import moe.fuqiuluo.http.entries.EmptyObject
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import moe.fuqiuluo.xposed.tools.asBoolean
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asJsonArray
import moe.fuqiuluo.xposed.tools.asJsonArrayOrNull
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObjectOrNull
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull

internal object ActionManager {
    val actionMap = mutableMapOf(
        "test" to TestHandler,
        "get_latest_events" to GetLatestEvents,
        "get_supported_actions" to GetSupportedActions,
        "get_status" to GetStatus,
        "get_version" to GetVersion,
        "get_self_info" to GetSelfInfo,
        "get_user_info" to GetProfileCard,
        "get_friend_list" to GetFriendList,
        "get_group_info" to GetTroopInfo,
        "get_group_list" to GetTroopList,
        "get_group_member_info" to GetTroopMemberInfo,
        "get_group_member_list" to GetTroopMemberList,
        "set_group_name" to ModifyTroopName,
        "leave_group" to LeaveTroop,
        "send_message" to SendMessage,
        "get_uid" to GetUid,
        "get_uin_by_uid" to GetUinByUid,
        "delete_message" to DeleteMessage,
        "sanc_qrcode" to ScanQRCode
    )

    operator fun get(action: String): IActionHandler? {
        return actionMap[action]
    }
}

internal abstract class IActionHandler {
    abstract suspend fun handle(session: ActionSession): String

    abstract fun path(): String

    inline fun <reified T> ok(data: T, msg: String = ""): String {
        return resultToString(true, Status.Ok, data!!, msg)
    }

    fun noParam(paramName: String): String {
        return failed(Status.BadParam, "lack of [$paramName]")
    }

    fun badParam(why: String): String {
        return failed(Status.BadParam, why)
    }

    fun error(why: String): String {
        return failed(Status.InternalHandlerError, why)
    }

    fun logic(why: String): String {
        return failed(Status.LogicError, why)
    }

    fun failed(status: Status, msg: String): String {
        return resultToString(false, status, EmptyObject, msg)
    }
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

    fun getStringOrNull(key: String): String? {
        return params[key].asStringOrNull
    }

    fun getBoolean(key: String): Boolean {
        return params[key].asBoolean
    }

    fun <T: Boolean?> getBooleanOrDefault(key: String, default: T? = null): T {
        return (params[key].asBooleanOrNull as? T) ?: default as T
    }

    fun getObject(key: String): JsonObject {
        return params[key].asJsonObject
    }

    fun getObjectOrNull(key: String): JsonObject? {
        return params[key].asJsonObjectOrNull
    }

    fun getArray(key: String): JsonArray {
        return params[key].asJsonArray
    }

    fun getArrayOrNull(key: String): JsonArray? {
        return params[key].asJsonArrayOrNull
    }

    fun has(key: String) = params.containsKey(key)
}