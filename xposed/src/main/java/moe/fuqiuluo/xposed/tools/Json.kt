package moe.fuqiuluo.xposed.tools

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

val EmptyJsonObject = JsonObject(mapOf())

val Collection<Any>.json: JsonArray
    get() {
        val arrayList = arrayListOf<JsonElement>()
        forEach {
            when(it) {
                is JsonElement -> arrayList.add(it)
                is Number -> arrayList.add(it.json)
                is String -> arrayList.add(it.json)
                is Boolean -> arrayList.add(it.json)
                is Map<*, *> -> {
                    val map = hashMapOf<String, JsonElement>()
                    it.forEach { (key, value) ->
                        when(value) {
                            is Number -> map[key.toString()] = value.json
                            is String -> map[key.toString()] = value.json
                            is Boolean -> map[key.toString()] = value.json
                            is JsonElement -> map[key.toString()] = value
                            else -> error("unknown object type: ${it::class.java}")
                        }
                    }
                    arrayList.add(JsonObject(map))
                }
                else -> error("unknown array type: ${it::class.java}")
            }
        }
        return arrayList.jsonArray
    }

val Collection<JsonElement>.jsonArray: JsonArray
    get() = JsonArray(this.toList())

val Boolean.json: JsonPrimitive
    get() = JsonPrimitive(this)

val String.json: JsonPrimitive
    get() = JsonPrimitive(this)

val Number.json: JsonPrimitive
    get() = JsonPrimitive(this)

val JsonElement?.asString: String
    get() = this!!.jsonPrimitive.content

val JsonElement?.asStringOrNull: String?
    get() = this?.jsonPrimitive?.content

val JsonElement?.asInt: Int
    get() = this!!.jsonPrimitive.int

val JsonElement?.asLong: Long
    get() = this!!.jsonPrimitive.long

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