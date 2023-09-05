package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.service.data.MessageResult
import moe.protocol.servlet.helper.MessageHelper
import moe.protocol.servlet.helper.ParamsException
import moe.protocol.servlet.protocol.MsgSvc
import kotlinx.serialization.json.JsonArray
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.json

internal object SendMessage: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val detailType = session.getString("detail_type")
        try {
            val chatType = MessageHelper.obtainMessageTypeByDetailType(detailType)
            val peerId = when(chatType) {
                MsgConstant.KCHATTYPEGROUP -> session.getStringOrNull("group_id") ?: return noParam("group_id")
                MsgConstant.KCHATTYPEC2C -> session.getStringOrNull("user_id") ?: return noParam("user_id")
                else -> error("unknown chat type: $chatType")
            }
            return if (session.isString("message")) {
                val autoEscape = session.getBooleanOrDefault("auto_escape", false)
                val message = session.getString("message")
                invoke(chatType, peerId, message, autoEscape)
            } else {
                val message = session.getArray("message")
                invoke(chatType, peerId, message)
            }
        } catch (e: ParamsException) {
            return noParam(e.message!!)
        } catch (e: Throwable) {
            return logic(e.message ?: e.toString())
        }
    }

    // 发送文本格式/CQ码类型消息
    suspend operator fun invoke(
        chatType: Int,
        peerId: String,
        message: String,
        autoEscape: Boolean
    ): String {
        val result = if (autoEscape) {
            MsgSvc.sendToAio(chatType, peerId, arrayListOf(message).json)
        } else {
            val msg = MessageHelper.decodeCQCode(message)
            if (msg.isEmpty()) {
                LogCenter.log("CQ码解码失败，CQ码不合法", Level.WARN)
                return logic("CQCdoe decode failed, CQCode is illegal")
            } else {
                MsgSvc.sendToAio(chatType, peerId, MessageHelper.decodeCQCode(message))
            }
        }
        return ok(
            MessageResult(
            msgId = result.second,
            time = result.first * 0.001
        )
        )
    }

    // 消息段格式消息
    suspend operator fun invoke(
        chatType: Int, peerId: String, message: JsonArray
    ): String {
        val result = MsgSvc.sendToAio(chatType, peerId, message)
        return ok(
            MessageResult(
            msgId = result.second,
            time = result.first * 0.001
        )
        )
    }

    override val requiredParams: Array<String> = arrayOf("detail_type", "message")

    override fun path(): String = "send_message"
}