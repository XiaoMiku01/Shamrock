@file:OptIn(DelicateCoroutinesApi::class)
package com.tencent.mobileqq.pushservice

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.data.MemberRole
import com.tencent.mobileqq.data.MsgSubType
import com.tencent.mobileqq.data.MsgType
import com.tencent.mobileqq.data.PushMessage
import com.tencent.mobileqq.data.Sender
import com.tencent.mobileqq.helper.ShamrockConfig
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.msg.toSegment
import com.tencent.qqnt.protocol.GroupSvc
import com.tencent.qqnt.protocol.MsgSvc
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.GlobalClient
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asIntOrNull
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import mqq.app.MobileQQ

internal object HttpPusher {
    private val actionMsgTypes = arrayOf(
        "record", "voice", "video", "markdown"
    )

    fun pushGroupMsg(record: MsgRecord, elements: List<MsgElement>, raw: String, msgHash: Int) {
        if (!ShamrockConfig.allowWebHook()) {
            return
        }
        GlobalScope.launch {
            val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
            val url = "http://" + ShamrockConfig.getWebHookAddress()
            try {
                val respond = GlobalClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(PushMessage(
                        time = record.msgTime,
                        selfId = app.longAccountUin,
                        postType = "message",
                        messageType = MsgType.Group,
                        subType = MsgSubType.NORMAL,
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
                            role = when (record.senderUin) {
                                GroupSvc.getOwner(record.peerUin.toString()) -> MemberRole.Owner
                                in GroupSvc.getAdminList(record.peerUin.toString()) -> MemberRole.Admin
                                else -> MemberRole.Member
                            },
                            title = "",
                            level = "",
                        )
                    ))
                }.bodyAsText()
                handleQuicklyReply(record, msgHash, respond)
            } catch (e: Throwable) {
                LogCenter.log("消息推送失败: ${e.stackTraceToString()}", Level.ERROR)
            }
        }
    }

    private suspend fun handleQuicklyReply(record: MsgRecord, msgHash: Int, jsonText: String) {
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
            if (data.containsKey("delete")) {
                TODO("快速操作: 撤回消息")
            }
            if (data.containsKey("kick")) {
                TODO("快速操作: 移出群聊")
            }
            if (data.containsKey("ban")) {
                val banTime = data["ban_duration"].asIntOrNull ?: 0
                if (banTime <= 0) return
                TODO("快速操作: 群聊禁言")
            }
        } catch (e: Throwable) {
            LogCenter.log("处理快速回复错: $e", Level.WARN)
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
                    MsgSvc.sendToAIO(record.chatType, record.peerUin.toString(), it.jsonArray)
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
        MsgSvc.sendToAIO(record.chatType, record.peerUin.toString(), when {
            else -> msgList
        }.jsonArray)
    }
}