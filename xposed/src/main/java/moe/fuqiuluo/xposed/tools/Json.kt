package moe.fuqiuluo.xposed.tools

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val JsonElement?.asString: String
    get() = this!!.jsonPrimitive.content

val JsonElement?.asInt: Int
    get() = this!!.jsonPrimitive.int

val JsonElement?.asJsonObject: JsonObject
    get() = this!!.jsonPrimitive.jsonObject

val JsonElement?.asJsonArray: JsonArray
    get() = this!!.jsonPrimitive.jsonArray