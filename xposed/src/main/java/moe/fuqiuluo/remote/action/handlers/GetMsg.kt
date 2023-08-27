package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.MessageDetail
import com.tencent.mobileqq.data.MessageSender
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.msg.MsgConvert
import com.tencent.qqnt.protocol.MsgSvc

internal object GetMsg: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val hashCode = session.getInt("message_id")
        return invoke(hashCode)
    }

    suspend operator fun invoke(msgHash: Int): String {
        val msgId = MessageHelper.getMsgIdByHashCode(msgHash)
        val msg = MsgSvc.getMsg(msgId)
            ?: return logic("Obtain msg failed, please check your msg_id.")

        return ok(MessageDetail(
            msg.msgTime.toInt(),
            MessageHelper.obtainDetailTypeByMsgType(msg.chatType),
            msgHash,
            msg.clientSeq.toInt(),
            MessageSender(
                msg.senderUin, msg.sendNickName, "unknown", 0, msg.senderUid
            ),
            MsgConvert.convertMsgRecordToMsgSegment(msg)
        ))
    }

    override val requiredParams: Array<String> = arrayOf("message_id")

    override fun path(): String = "get_msg"
}