package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonArray
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.data.MessageResult
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.http.entries.EmptyObject
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import moe.fuqiuluo.xposed.helper.DataRequester
import mqq.app.MobileQQ
import kotlin.coroutines.resume
import kotlin.properties.Delegates

internal object SendMessage: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        when(val chatType = MessageHelper.obtainMessageTypeByDetailType(
            session.getStringOrNull("detail_type") ?: return noParam("detail_type")
        )) {
            MsgConstant.KCHATTYPEGROUP -> {
                val groupId = session.getStringOrNull("group_id") ?: return noParam("group_id")
                val message = session.getArrayOrNull("message") ?: return noParam("message")
                val result = sendToTroop(groupId, message)
                return ok(MessageResult(
                    msgId = result.second,
                    time = result.first * 0.001
                ))
            }
        }
        return logic("unable to send message: not support detail_type")
    }

    private suspend fun sendToTroop(groupId: String, message: JsonArray): Pair<Long, Long> {
        return MessageHelper.sendTroopMessage(
            groupId = groupId,
            msgElements = MessageHelper.messageArrayToMessageElements(
                chatType = MsgConstant.KCHATTYPEGROUP,
                targetUin = groupId,
                messageList = message
            ).also {
                if (it.isNullOrEmpty()) error("message is empty, unable to send")
            }
        ) { code, _ ->
            DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                put("string", "消息发送 troop: $groupId, code: $code")
            })
        }
    }

    override fun path(): String = "send_message"
}