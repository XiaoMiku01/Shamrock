package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.serialization.json.JsonArray
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.data.MessageResult
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.http.action.helper.msg.InternalMessageMakerError
import moe.fuqiuluo.http.action.helper.msg.ParamsException
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import mqq.app.MobileQQ

internal object SendMessage: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val detailType = session.getStringOrNull("detail_type") ?: return noParam("detail_type")
        kotlin.runCatching {
            when(val chatType = MessageHelper.obtainMessageTypeByDetailType(detailType)) {
                MsgConstant.KCHATTYPEGROUP -> {
                    val groupId = session.getStringOrNull("group_id") ?: return noParam("group_id")
                    val message = session.getArrayOrNull("message") ?: return noParam("message")
                    val result = sendToTroop(groupId, message)
                    return ok(MessageResult(
                        msgId = result.second,
                        time = result.first * 0.001
                    ))
                }

                else -> {}
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

    private suspend fun sendToTroop(groupId: String, message: JsonArray): Pair<Long, Long> {
        return MessageHelper.sendTroopMessage(
            groupId = groupId,
            msgElements = MessageHelper.messageArrayToMessageElements(
                chatType = MsgConstant.KCHATTYPEGROUP,
                targetUin = groupId,
                messageList = message
            ).also {
                if (it.isEmpty()) kotlin.error("message is empty, unable to send")
            }
        ) { code, _ ->
            LogCenter.log("消息发送 troop: $groupId, code: $code")
        }
    }

    override fun path(): String = "send_message"
}