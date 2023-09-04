@file:OptIn(DelicateCoroutinesApi::class)
package com.tencent.mobileqq.pushservice

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.data.push.MemberRole
import com.tencent.mobileqq.data.push.MsgSubType
import com.tencent.mobileqq.data.push.MsgType
import com.tencent.mobileqq.data.push.PushMessage
import com.tencent.mobileqq.data.push.Sender
import com.tencent.mobileqq.helper.ShamrockConfig
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.msg.toSegment
import com.tencent.qqnt.protocol.GroupSvc
import com.tencent.qqnt.protocol.MsgSvc
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
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
import moe.fuqiuluo.xposed.tools.asBoolean
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asIntOrNull
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import mqq.app.MobileQQ
import java.net.SocketException

internal object HttpPusher {
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

    fun pushNotice() {
        if (!ShamrockConfig.allowWebHook()) {
            return
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
        if (!ShamrockConfig.allowWebHook()) {
            return
        }
        GlobalScope.launch {
            val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
            val url = "http://" + ShamrockConfig.getWebHookAddress()
            try {
                val respond = GlobalClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
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
                    )
                }.bodyAsText()
                handleQuicklyReply(record, msgHash, respond)
            } catch (e: ConnectTimeoutException) {
                LogCenter.log("消息推送失败: 请检查你的推送服务器。", Level.ERROR)
            } catch (e: SocketException) {
                LogCenter.log("消息推送失败: 网络波动。", Level.ERROR)
            } catch (e: HttpRequestTimeoutException) {
                LogCenter.log("消息推送失败: 推送服务器无法连接。", Level.ERROR)
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