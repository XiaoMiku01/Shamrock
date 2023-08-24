package com.tencent.qqnt.helper

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.msg.api.IMsgService
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import com.tencent.qqnt.msg.InternalMessageMakerError
import com.tencent.qqnt.msg.MessageMaker
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.MMKVFetcher
import moe.fuqiuluo.xposed.tools.EmptyJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObjectOrNull
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import oicq.wlogin_sdk.tools.MD5
import kotlin.math.abs

internal object MessageHelper {
    suspend fun sendMessageWithoutMsgId(chatType: Int, peerId: String, message: JsonArray, callback: IOperateCallback): Pair<Long, Int> {
        val service = QRoute.api(IMsgService::class.java)
        var uniseq = generateMsgId(chatType, peerId.toLong())
        var nonMsg: Boolean
        val msg = messageArrayToMessageElements(chatType, uniseq.second, peerId, message).also {
            if (it.isEmpty()) error("message is empty, unable to send")
        }.filter {
            it.elementType != -1
        }.also {
            nonMsg = it.isEmpty()
        }
        if (!nonMsg) {
            service.sendMsg(
                generateContact(chatType, peerId),
                uniseq.second,
                msg as ArrayList<MsgElement>,
                callback
            )
        } else {
            uniseq = 0 to 0
        }
        return System.currentTimeMillis() to uniseq.first
    }

    suspend fun generateContact(chatType: Int, id: String, subId: String = ""): Contact {
        val peerId = if (MsgConstant.KCHATTYPEC2C == chatType) {
           ContactHelper.getUidByUinAsync(id.toLong())
        } else id
        return Contact(chatType, peerId, subId)
    }

    fun obtainMessageTypeByDetailType(detailType: String): Int {
        return when(detailType) {
            "troop", "group" -> MsgConstant.KCHATTYPEGROUP
            "private" -> MsgConstant.KCHATTYPEC2C
            "less" -> MsgConstant.KCHATTYPETEMPC2CFROMUNKNOWN
            "guild" -> MsgConstant.KCHATTYPEGUILD
            else -> error("不支持的消息来源类型")
        }
    }

    fun obtainDetailTypeByMsgType(msgType: Int): String {
        return when(msgType) {
            MsgConstant.KCHATTYPEGROUP -> "group"
            MsgConstant.KCHATTYPEC2C -> "private"
            MsgConstant.KCHATTYPEGUILD -> "guild"
            MsgConstant.KCHATTYPETEMPC2CFROMUNKNOWN -> "less"
            else -> error("不支持的消息来源类型")
        }
    }

    suspend fun messageArrayToMessageElements(chatType: Int, msgId: Long, targetUin: String, messageList: JsonArray): ArrayList<MsgElement> {
        val msgList = arrayListOf<MsgElement>()
        messageList.forEach {
            val msg = it.jsonObject
            kotlin.runCatching {
                val maker = MessageMaker[msg["type"].asString]
                if(maker != null) {
                    val data = msg["data"].asJsonObjectOrNull ?: EmptyJsonObject
                    msgList.add(maker(chatType, msgId, targetUin, data))
                }
            }.onFailure {
                if (it is InternalMessageMakerError) {
                    throw it
                }
                LogCenter.log(it.stackTraceToString(), Level.ERROR)
            }
        }
        return msgList
    }

    fun generateMsgIdHash(chatType: Int, msgId: Long): Int {
        return when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> {
                val key = "troop$msgId"
                abs(MD5.getMD5String(key.toByteArray()).hashCode())
            }
            MsgConstant.KCHATTYPEC2C -> {
                val key = "c2c$msgId"
                abs(MD5.getMD5String(key.toByteArray()).hashCode())
            }
            else -> error("不支持的消息来源类型 | generateMsgIdHash: $chatType")
        }
    }

    fun generateMsgId(chatType: Int, peerId: Long): Pair<Int, Long> {
        val msgId = createMessageUniseq(chatType, System.currentTimeMillis())
        val hashCode: Int = generateMsgIdHash(chatType, msgId)
        val mmkv = MMKVFetcher.defaultMMKV()
        when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> {
                mmkv.putLong("troop$msgId", peerId)
                mmkv.putLong(hashCode.toString(), msgId)
            }
            MsgConstant.KCHATTYPEC2C -> {
                mmkv.putLong("c2c$msgId", peerId)
                mmkv.putLong(hashCode.toString(), msgId)
            }
            else -> {
                error("不支持的消息来源类型: $chatType, $peerId")
            }
        }
        return hashCode to msgId
    }

    fun removeMsgByHashCode(hashCode: Int) {
        val msgId = getMsgIdByHashCode(hashCode)
        val chatType = getChatType(msgId)
        val mmkv = MMKVFetcher.defaultMMKV()
        mmkv.remove(hashCode.toString())
        when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> mmkv.remove("troop$msgId")
            MsgConstant.KCHATTYPEC2C -> mmkv.remove("c2c$msgId")
            else -> error("暂时不支持该类型消息: $chatType")
        }
    }

    fun getMsgIdByHashCode(hashCode: Int): Long {
        return MMKVFetcher.defaultMMKV().getLong(hashCode.toString(), 0)
    }

    external fun createMessageUniseq(chatType: Int, time: Long): Long

    fun decodeCQCode(code: String): JsonArray {
        val arrayList = ArrayList<JsonElement>()
        val msgList = nativeDecodeCQCode(code)
        msgList.forEach {
            val params = hashMapOf<String, JsonElement>()
            it.forEach { (key, value) ->
                if (key != "_type") {
                    params[key] = value.json
                }
            }
            val data = hashMapOf(
                "type" to it["_type"]!!.json,
                "data" to JsonObject(params)
            )
            arrayList.add(JsonObject(data))
        }
        return arrayList.jsonArray
    }

    fun encodeCQCode(msg: ArrayList<HashMap<String, JsonElement>>): String {
        return nativeEncodeCQCode(msg.map {
            val params = hashMapOf<String, String>()
            it.forEach { (key, value) ->
                if (key != "type") {
                    value.asJsonObject.forEach { param, element ->
                        params[param] = element.asString
                    }
                } else {
                    params["_type"] = value.asString
                }
            }
            params
        })
    }

    fun getPeerIdByMsgId(msgId: Long): Long {
        val chatType = getChatType(msgId)
        val mmkv = MMKVFetcher.defaultMMKV()
        return when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> mmkv.getLong("troop$msgId", 0)
            MsgConstant.KCHATTYPEC2C -> mmkv.getLong("c2c$msgId", 0)
            else -> error("暂时不支持该类型消息: $chatType")
        }
    }

    private external fun nativeDecodeCQCode(code: String): List<Map<String, String>>
    private external fun nativeEncodeCQCode(segment: List<Map<String, String>>): String

    external fun getChatType(msgId: Long): Int
}