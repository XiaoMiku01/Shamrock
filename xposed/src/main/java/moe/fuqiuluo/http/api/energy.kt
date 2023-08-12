package moe.fuqiuluo.http.api

import com.tencent.mobileqq.qsec.qsecdandelionsdk.Dandelion
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.xposed.tools.fetchGet
import moe.fuqiuluo.xposed.tools.fetchGetOrNull
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import moe.fuqiuluo.xposed.tools.toHexString
import java.nio.ByteBuffer

fun Routing.energy() {
    get("/custom_energy") {
        val data = fetchGet("data")
        val salt = fetchGet("salt").hex2ByteArray()

        val sign = Dandelion.getInstance().fly(data, salt)

        call.respond(OldApiResult(0, "success", sign.toHexString()))
    }

    get("/energy") {
        val data = fetchGet("data")
        if(!(data.startsWith("810_") || data.startsWith("812_"))) {
            call.respond(OldApiResult(-2, "data参数不合法", null))
            return@get
        }

        var mode = fetchGetOrNull("mode")
        if (mode == null) {
            mode = when(data) {
                "810_d", "810_a", "810_f", "810_9" -> "v2"
                "810_2", "810_25", "810_7", "810_24" -> "v1"
                "812_a" -> "v3"
                "812_5" -> "v4"
                else -> null
            }
        }
        if (mode == null) {
            call.respond(OldApiResult(-2, "无法自动决断mode，请主动提供", null))
            return@get
        }

        val salt = when (mode) {
            "v1" -> {
                val uin = fetchGet("uin").toLong()
                val version = fetchGetOrNull("version") ?: return@get
                val guid = (fetchGetOrNull("guid") ?: return@get).hex2ByteArray()
                val salt = ByteBuffer.allocate(8 + 2 + guid.size + 2 + 10)
                salt.putLong(uin)
                salt.putShort(guid.size.toShort())
                salt.put(guid)
                salt.putShort(version.length.toShort())
                salt.put(version.toByteArray())
                salt.array()
            }
            "v2" -> {
                val version = fetchGetOrNull("version") ?: return@get
                val guid = (fetchGetOrNull("guid") ?: return@get).hex2ByteArray()
                val sub = data.substring(4).toInt(16)
                val salt = ByteBuffer.allocate(4 + 2 + guid.size + 2 + 10 + 4 + 4)
                salt.putInt(0)
                salt.putShort(guid.size.toShort())
                salt.put(guid)
                salt.putShort(version.length.toShort())
                salt.put(version.toByteArray())
                salt.putInt(sub)
                salt.putInt(0)
                salt.array()
            }
            "v3" -> { // 812_a
                val version = fetchGetOrNull("version") ?: return@get
                val phone = (fetchGetOrNull("phone") ?: return@get).toByteArray() // 86-xxx
                val salt = ByteBuffer.allocate(phone.size + 2 + 2 + version.length + 2)
                // 38 36 2D 31 37 33 36 30 32 32 39 31 37 32
                // 00 00
                // 00 06
                // 38 2E 39 2E 33 38
                // 00 00
                // result => 0C051B17347DF3B8EFDE849FC233C88DBEA23F5277099BB313A9CD000000004B744F7A00000000
                salt.put(phone)
                //println(String(phone))
                salt.putShort(0)
                salt.putShort(version.length.toShort())
                salt.put(version.toByteArray())
                salt.putShort(0)
                salt.array()
            }
            "v4" -> { // 812_5
               error("Not support [v4] mode.")
            }
            else -> ByteArray(0)
        }

        val sign = Dandelion.getInstance().fly(data, salt)
        call.respond(OldApiResult(0, "success", sign.toHexString()))
    }
}