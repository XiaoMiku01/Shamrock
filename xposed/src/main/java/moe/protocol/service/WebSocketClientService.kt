@file:OptIn(DelicateCoroutinesApi::class)

package moe.protocol.service

import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.tools.json
import moe.protocol.service.api.WebSocketClientServlet
import moe.protocol.service.data.push.MemberRole
import moe.protocol.service.data.push.MsgSubType
import moe.protocol.service.data.push.MsgType
import moe.protocol.service.data.push.NoticeSubType
import moe.protocol.service.data.push.NoticeType
import moe.protocol.service.data.push.PushMessage
import moe.protocol.service.data.push.PushNotice
import moe.protocol.service.data.push.Sender
import moe.protocol.service.config.ShamrockConfig
import moe.protocol.servlet.msg.toSegment
import moe.protocol.servlet.protocol.GroupSvc

internal object WebSocketClientService: WebSocketClientServlet() {
    override fun pushPrivateMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int
    ) {
        pushMsg(record, elements, raw, msgHash, MsgType.Private, MsgSubType.Friend)
    }

    override fun pushGroupMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int
    ) {
        pushMsg(
            record, elements, raw, msgHash, MsgType.Group, MsgSubType.NORMAL,
            role = when (record.senderUin) {
                GroupSvc.getOwner(record.peerUin.toString()) -> MemberRole.Owner
                in GroupSvc.getAdminList(record.peerUin.toString()) -> MemberRole.Admin
                else -> MemberRole.Member
            }
        )
    }

    override fun pushGroupPoke(time: Long, operation: Long, userId: Long, groupId: Long) {
        pushNotice(
            time = time,
            type = NoticeType.Notify,
            subType = NoticeSubType.Poke,
            operation = operation,
            userId = operation,
            groupId = groupId,
            target = userId
        )
    }

    override fun pushPrivateMsgRecall(time: Long, operation: Long, msgHash: Long, tip: String) {
        pushNotice(
            time = time,
            type = NoticeType.FriendRecall,
            operation = operation,
            userId = operation,
            msgId = msgHash,
            tip = tip
        )
    }

    override fun pushGroupMsgRecall(
        time: Long,
        operation: Long,
        userId: Long,
        groupId: Long,
        msgHash: Long,
        tip: String
    ) {
        pushNotice(
            time = time,
            type = NoticeType.GroupRecall,
            operation = operation,
            userId = userId,
            groupId =  groupId,
            msgId = msgHash,
            tip = tip
        )
    }

    override fun pushGroupBan(
        time: Long,
        operation: Long,
        userId: Long,
        groupId: Long,
        duration: Int
    ) {
        pushNotice(time, NoticeType.GroupBan, if (duration == 0) NoticeSubType.LiftBan else NoticeSubType.Ban, operation, userId, groupId, duration)
    }

    override fun pushGroupMemberDecreased(
        time: Long,
        target: Long,
        groupId: Long,
        operation: Long,
        type: NoticeType,
        subType: NoticeSubType
    ) {
        pushNotice(time, type, subType, operation, target, groupId)
    }

    override fun pushGroupAdminChange(time: Long, target: Long, groupId: Long, setAdmin: Boolean) {
        pushNotice(time, NoticeType.GroupAdminChange, if (setAdmin) NoticeSubType.Set else NoticeSubType.UnSet, 0, target, groupId)
    }

    private fun pushNotice(
        time: Long,
        type: NoticeType,
        subType: NoticeSubType = NoticeSubType.Set,
        operation: Long,
        userId: Long,
        groupId: Long = 0,
        duration: Int = 0,
        msgId: Long = 0,
        target: Long = 0,
        tip: String = ""
    ) {
        pushTo(
            PushNotice(
                time = time,
                selfId = app.longAccountUin,
                postType = "notice",
                type = type,
                subType = subType,
                operatorId = operation,
                userId = userId,
                groupId = groupId,
                duration = duration,
                target = target,
                msgId = msgId,
                tip = tip
            )
        )
    }

    private fun pushMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int,
        msgType: MsgType,
        subType: MsgSubType,
        role: MemberRole = MemberRole.Member
    ) {
        GlobalScope.launch {
            pushTo(
                PushMessage(
                    time = record.msgTime,
                    selfId = app.longAccountUin,
                    postType = "message",
                    messageType = msgType,
                    subType = subType,
                    messageId = msgHash,
                    groupId = record.peerUin,
                    userId = record.senderUin,
                    message = if (ShamrockConfig.useCQ()) raw.json else elements.toSegment(record.chatType).json,
                    rawMessage = raw,
                    font = 0,
                    sender = Sender(
                        userId = record.senderUin,
                        nickname = record.sendNickName,
                        card = record.sendMemberName,
                        role = role,
                        title = "",
                        level = "",
                    )
                )
            )
        }
    }
}