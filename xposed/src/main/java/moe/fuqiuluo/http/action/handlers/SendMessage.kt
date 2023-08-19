package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.serialization.json.JsonArray
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.data.MessageResult
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.http.action.helper.msg.InternalMessageMakerError
import moe.fuqiuluo.http.action.helper.msg.ParamsException
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.json

internal object SendMessage: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val detailType = session.getStringOrNull("detail_type") ?: return noParam("detail_type")
        kotlin.runCatching {
            val chatType = MessageHelper.obtainMessageTypeByDetailType(detailType)
            val peerId = when(chatType) {
                MsgConstant.KCHATTYPEGROUP -> session.getStringOrNull("group_id") ?: return noParam("group_id")
                MsgConstant.KCHATTYPEC2C -> session.getStringOrNull("user_id") ?: return noParam("user_id")
                else -> error("unknown chat type: $chatType")
            }
            if (session.isString("message")) {
                // CQ码 | 纯文本
                val autoEscape = session.getBooleanOrDefault("auto_escape", false)
                val message = session.getString("message")
                if (autoEscape) {
                    val result = sendToAIO(chatType, peerId, arrayListOf(message).json)
                    return ok(MessageResult(
                        msgId = result.second,
                        time = result.first * 0.001
                    ))
                } else {
                    val msg = MessageHelper.decodeCQCode(message)
                    if (msg.isEmpty()) {
                        LogCenter.log("CQ码解码失败，CQ码不合法")
                    } else {
                        LogCenter.log(msg.toString())
                        val result = sendToAIO(chatType, peerId, MessageHelper.decodeCQCode(message))
                        return ok(MessageResult(
                            msgId = result.second,
                            time = result.first * 0.001
                        ))
                    }
                }
            } else {
                // 消息段
                val message = session.getArrayOrNull("message") ?: return noParam("message")
                val result = sendToAIO(chatType, peerId, message)
                return ok(MessageResult(
                    msgId = result.second,
                    time = result.first * 0.001
                ))
            }
        }.onFailure {
            return if (it is InternalMessageMakerError) {
                if (it is ParamsException) {
                    noParam(it.message!!)
                } else {
                    error(it.message!!)
                }
            } else {
                error(it.message ?: "unknown error")
            }
        }
        return logic("unable to send message: not support $detailType")
    }

    private suspend fun sendToAIO(chatType: Int, peedId: String, message: JsonArray): Pair<Long, Int> {
        return MessageHelper.sendMessageWithoutMsgId(chatType, peedId, message, MessageCallback(peedId))
    }

    class MessageCallback(
        private val peerId: String
    ): IOperateCallback {
        override fun onResult(code: Int, reason: String?) {
            LogCenter.log("消息发送: $peerId, code: $code $reason")
        }
    }

    override fun path(): String = "send_message"
}