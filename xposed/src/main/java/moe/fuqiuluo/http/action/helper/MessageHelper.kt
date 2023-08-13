package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.msg.api.IMsgService
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import moe.fuqiuluo.http.action.helper.msg.MessageMaker
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import kotlin.math.abs
import kotlin.random.Random

internal object MessageHelper {
    fun sendTroopMessage(groupId: String, msgElements: ArrayList<MsgElement>, callback: IOperateCallback): Pair<Long, Long> {
        val service = QRoute.api(IMsgService::class.java)
        val time = System.currentTimeMillis()
        val uniseq = createMessageUniseq(time)
        service.sendMsg(
            Contact(MsgConstant.KCHATTYPEGROUP, groupId, ""), uniseq, msgElements, callback
        )
        return time to uniseq
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
            val maker = MessageMaker[msg["type"].asString]
            if(maker != null) {
                val data = msg["data"].asJsonObject
                msgList.add(maker(chatType, targetUin, data))
            }
        }
        return msgList
    }

    private fun createMessageUniseq(time: Long): Long {
        var uniseq = (time / 1000).toInt().toLong()
        uniseq = uniseq shl 32 or abs(Random.nextInt()).toLong()
        return uniseq
    }
}