package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.msg.api.IMsgService
import de.robv.android.xposed.XposedBridge
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import moe.fuqiuluo.http.action.helper.msg.InternalMessageMakerError
import moe.fuqiuluo.http.action.helper.msg.MessageMaker
import moe.fuqiuluo.xposed.helper.MMKVHelper
import moe.fuqiuluo.xposed.tools.EmptyJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asJsonObjectOrNull
import moe.fuqiuluo.xposed.tools.asString
import kotlin.math.abs
import kotlin.random.Random

internal object MessageHelper {
    fun sendTroopMessage(groupId: String, msgElements: ArrayList<MsgElement>, callback: IOperateCallback): Pair<Long, Long> {
        val service = QRoute.api(IMsgService::class.java)
        val uniseq = generateMsgId(MsgConstant.KCHATTYPEGROUP, groupId.toLong())
        service.sendMsg(
            generateContact(MsgConstant.KCHATTYPEGROUP, groupId),
            uniseq,
            msgElements,
            callback
        )
        return System.currentTimeMillis() to uniseq
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

    suspend fun messageArrayToMessageElements(chatType: Int, targetUin: String, messageList: JsonArray): ArrayList<MsgElement> {
        val msgList = arrayListOf<MsgElement>()
        messageList.forEach {
            val msg = it.jsonObject
            kotlin.runCatching {
                val maker = MessageMaker[msg["type"].asString]
                if(maker != null) {
                    val data = msg["data"].asJsonObjectOrNull ?: EmptyJsonObject
                    msgList.add(maker(chatType, targetUin, data))
                }
            }.onFailure {
                if (it is InternalMessageMakerError) {
                    throw it
                }
                XposedBridge.log(it)
            }
        }
        return msgList
    }

    fun generateMsgId(chatType: Int, peerId: Long): Long {
        val msgId = createMessageUniseq(chatType, System.currentTimeMillis())
        val mmkv = MMKVHelper.defaultMMKV()
        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            mmkv.putLong("troop_$msgId", peerId)
        }
        return msgId
    }

    private external fun createMessageUniseq(chatType: Int, time: Long): Long

    external fun getChatType(msgId: Long): Int
}