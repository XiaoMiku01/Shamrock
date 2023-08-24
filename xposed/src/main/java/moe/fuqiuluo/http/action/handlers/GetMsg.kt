package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.msg.api.IMsgService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.MessageDetail
import com.tencent.mobileqq.data.MessageSender
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.msg.MsgConvert
import kotlin.coroutines.resume

internal object GetMsg: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val hashCode = session.getIntOrNull("message_id") ?: return noParam("message_id")
        val msgId = MessageHelper.getMsgIdByHashCode(hashCode)
        val chatType = MessageHelper.getChatType(msgId)
        val peerId = MessageHelper.getPeerIdByMsgId(msgId)
        val contact = MessageHelper.generateContact(chatType, peerId.toString())
        val msg = withTimeout(5000) {
            val service = QRoute.api(IMsgService::class.java)
            suspendCancellableCoroutine<MsgRecord?> { continuation ->
                service.getMsgsByMsgId(contact, arrayListOf(msgId)) { code, _, msgRecords ->
                    if (code == 0 && msgRecords.isNotEmpty()) {
                        continuation.resume(msgRecords.first())
                    } else {
                        continuation.resume(null)
                    }
                }
                continuation.invokeOnCancellation {
                    continuation.resume(null)
                }
            }
        } ?: return logic("Obtain msg failed, please check your msg_id.")

        return ok(
            MessageDetail(
            msg.msgTime.toInt(),
            MessageHelper.obtainDetailTypeByMsgType(chatType),
            hashCode,
            msg.clientSeq.toInt(),
            MessageSender(
                msg.senderUin, msg.sendNickName, "unknown", 0, msg.senderUid
            ),
            MsgConvert.convertMsgRecordToMsgSegment(msg)
        )
        )
    }



    override fun path(): String = "get_msg"
}