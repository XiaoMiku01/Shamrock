package moe.fuqiuluo.http.action.helper

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
import moe.fuqiuluo.http.action.helper.msg.InternalMessageMakerError
import moe.fuqiuluo.http.action.helper.msg.MessageMaker
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.MMKVFetcher
import moe.fuqiuluo.xposed.tools.EmptyJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObjectOrNull
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.nextInt

internal object MessageHelper {
    suspend fun sendTroopMessage(groupId: String, message: JsonArray, callback: IOperateCallback): Pair<Long, Int> {
        val service = QRoute.api(IMsgService::class.java)
        var uniseq = generateMsgId(MsgConstant.KCHATTYPEGROUP, groupId.toLong())
        var nonMsg: Boolean
        val msg = messageArrayToMessageElements(MsgConstant.KCHATTYPEGROUP, uniseq.second, groupId, message).also {
            if (it.isEmpty()) error("message is empty, unable to send")
        }.filter {
            it.elementType != -1
        }.also {
            nonMsg = it.isEmpty()
        }
        if (!nonMsg) {
            service.sendMsg(
                generateContact(MsgConstant.KCHATTYPEGROUP, groupId),
                uniseq.second,
                msg as ArrayList<MsgElement>,
                callback
            )
        } else {
            uniseq = 0 to 0
        }
        return System.currentTimeMillis() to uniseq.first
    }

    fun generateContact(chatType: Int, id: String, subId: String = ""): Contact {
        return Contact(chatType, id, subId)
    }

    fun obtainMessageTypeByDetailType(detailType: String): Int {
        return when(detailType) {
            "troop", "group" -> MsgConstant.KCHATTYPEGROUP
            "private" -> MsgConstant.KCHATTYPEC2C
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

    fun generateMsgId(chatType: Int, peerId: Long): Pair<Int, Long> {
        val msgId = createMessageUniseq(chatType, System.currentTimeMillis())
        val hashCode: Int
        val mmkv = MMKVFetcher.defaultMMKV()
        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            val key = "troop_$msgId"
            hashCode = abs(key.hashCode()) + Random.nextInt(0 .. 10000)
            mmkv.putLong(key, peerId)
            mmkv.putLong(hashCode.toString(), msgId)
        } else {
            error("不支持的消息来源类型: $chatType, $peerId")
        }
        return hashCode to msgId
    }

    fun getMsgIdByHashCode(hashCode: Int): Long {
        return MMKVFetcher.defaultMMKV().getLong(hashCode.toString(), 0)
    }

    private external fun createMessageUniseq(chatType: Int, time: Long): Long

    fun decodeCQCode(code: String): JsonArray {
        val arrayList = ArrayList<JsonElement>()
        val msgList = nativeDecodeCQCode(code)
        msgList.forEach {
            val params = hashMapOf<String, JsonElement>()
            it.forEach { (key, value) ->
                if (key != "type") {
                    params[key] = value.json
                }
            }
            val data = hashMapOf(
                "type" to it["type"]!!.json,
                "data" to JsonObject(params)
            )
            arrayList.add(JsonObject(data))
        }
        return arrayList.jsonArray
    }

    private external fun nativeDecodeCQCode(code: String): List<Map<String, String>>

    external fun getChatType(msgId: Long): Int
}