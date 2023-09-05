@file:OptIn(DelicateCoroutinesApi::class)
package moe.protocol.service

import moe.protocol.service.data.push.MemberRole
import moe.protocol.service.data.push.MsgSubType
import moe.protocol.service.data.push.MsgType
import moe.protocol.service.data.push.NoticeSubType
import moe.protocol.service.data.push.NoticeType
import moe.protocol.service.data.push.PushMessage
import moe.protocol.service.data.push.PushNotice
import moe.protocol.service.data.push.Sender
import moe.protocol.service.helper.ShamrockConfig
import moe.protocol.servlet.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import moe.protocol.servlet.msg.toSegment
import moe.protocol.servlet.protocol.GroupSvc
import moe.protocol.servlet.protocol.MsgSvc
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.asBoolean
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asIntOrNull
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import moe.protocol.service.api.HttpPushServlet

internal object HttpService: HttpPushServlet() {
    private val actionMsgTypes = arrayOf(
        "record", "voice", "video", "markdown"
    )

    fun pushPrivateMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int
    ) {
        pushMsg(record, elements, raw, msgHash, MsgType.Private, MsgSubType.Friend)
    }

    fun pushGroupMsg(
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

    fun pushGroupPoke(time: Long, operation: Long, userId: Long, groupId: Long) {
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

    fun pushPrivateMsgRecall(time: Long, operation: Long, msgHash: Long, tip: String) {
        pushNotice(
            time = time,
            type = NoticeType.FriendRecall,
            operation = operation,
            userId = operation,
            msgId = msgHash,
            tip = tip
        )
    }

    fun pushGroupMsgRecall(
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

    fun pushGroupBan(
        time: Long,
        operation: Long,
        userId: Long,
        groupId: Long,
        duration: Int
    ) {
        pushNotice(time, NoticeType.GroupBan, if (duration == 0) NoticeSubType.LiftBan else NoticeSubType.Ban, operation, userId, groupId, duration)
    }

    fun pushGroupMemberDecreased(
        time: Long,
        target: Long,
        groupId: Long,
        operation: Long = 0,
        type: NoticeType,
        subType: NoticeSubType
    ) {
        pushNotice(time, type, subType, operation, target, groupId)
    }

    fun pushGroupAdminChange(time: Long, target: Long, groupId: Long, setAdmin: Boolean) {
        pushNotice(time, NoticeType.GroupAdminChange, if (setAdmin) NoticeSubType.Set else NoticeSubType.UnSet, 0, target, groupId)
    }

    fun pushNotice(
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
        GlobalScope.launch {
            pushTo(PushNotice(
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
            ))
        }
    }

    fun pushMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int,
        msgType: MsgType,
        subType: MsgSubType,
        role: MemberRole = MemberRole.Member
    ) {
        GlobalScope.launch {
            val respond = pushTo(
                PushMessage(
                time = record.msgTime,
                selfId = app.longAccountUin,
                postType = "message",
                messageType = msgType,
                subType = subType,
                messageId = msgHash,
                groupId = record.peerUin,
                userId = record.senderUin,
                message = if(ShamrockConfig.useCQ()) raw.json else elements.toSegment(record.chatType).json,
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
            ) ?: return@launch
            handleQuicklyReply(record, respond.bodyAsText())
        }
    }

    private suspend fun handleQuicklyReply(record: MsgRecord, jsonText: String) {
        try {
            val data = Json.parseToJsonElement(jsonText).asJsonObject
            if (data.containsKey("reply")) {
                val autoEscape = data["auto_escape"].asBooleanOrNull ?: false
                val atSender = data["at_sender"].asBooleanOrNull ?: false
                val message = data["reply"]
                if (message is JsonPrimitive) {
                    if (autoEscape) {
                        val msgList = mutableSetOf<JsonElement>()
                        msgList.add(mapOf(
                            "type" to "text",
                            "data" to mapOf(
                                "text" to message.asString
                            )
                        ).json)
                        quicklyReply(record, msgList.jsonArray, atSender)
                    } else {
                        val messageArray = MessageHelper.decodeCQCode(message.asString)
                        quicklyReply(record, messageArray, atSender)
                    }
                } else if (message is JsonArray) {
                    quicklyReply(record, message, atSender)
                }
            }
            if (data.containsKey("delete") && data["delete"].asBoolean) {
                MsgSvc.recallMsg(record.msgId)
            }
            if (data.containsKey("kick") && data["kick"].asBoolean) {
                GroupSvc.kickMember(record.peerUin, false, record.senderUin)
            }
            if (data.containsKey("ban") && data["ban"].asBoolean) {
                val banTime = data["ban_duration"].asIntOrNull ?: (30 * 60)
                if (banTime <= 0) return
                GroupSvc.banMember(record.peerUin, record.senderUin, banTime)
            }
        } catch (e: Throwable) {
            LogCenter.log("处理快速操作错误: $e", Level.WARN)
        }
    }

    private suspend fun quicklyReply(
        record: MsgRecord,
        message: JsonArray,
        atSender: Boolean,
    ) {
        val msgList = mutableSetOf<JsonElement>()
        message.filter {
            it.asJsonObject["type"]?.asString in actionMsgTypes
        }.let {
            if (it.isNotEmpty()) {
                it.map { listOf(it) }.forEach {
                    MsgSvc.sendToAio(record.chatType, record.peerUin.toString(), it.jsonArray)
                }
                return
            }
        }

        msgList.add(mapOf(
            "type" to "reply",
            "data" to mapOf(
                "id" to record.msgId
            )
        ).json) // 添加回复
        if (atSender) {
            msgList.add(mapOf(
                "type" to "at",
                "data" to mapOf(
                    "qq" to record.senderUin
                )
            ).json) // 添加@发送者
        }
        msgList.addAll(message)
        MsgSvc.sendToAio(record.chatType, record.peerUin.toString(), when {
            else -> msgList
        }.jsonArray)
    }
}