package moe.fuqiuluo.xposed.tools

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val EmptyJsonObject = JsonObject(mapOf())

val JsonElement?.asString: String
    get() = this!!.jsonPrimitive.content

val JsonElement?.asStringOrNull: String?
    get() = this?.jsonPrimitive?.content

val JsonElement?.asInt: Int
    get() = this!!.jsonPrimitive.int

val JsonElement?.asIntOrNull: Int?
    get() = this?.jsonPrimitive?.int

val JsonElement?.asBoolean: Boolean
    get() = this!!.jsonPrimitive.boolean

val JsonElement?.asBooleanOrNull: Boolean?
    get() = this?.jsonPrimitive?.booleanOrNull

val JsonElement?.asJsonObject: JsonObject
    get() = this!!.jsonObject

val JsonElement?.asJsonObjectOrNull: JsonObject?
    get() = this?.jsonObject

val JsonElement?.asJsonArray: JsonArray
    get() = this!!.jsonArray

val JsonElement?.asJsonArrayOrNull: JsonArray?
    get() = this?.jsonArray