package com.tencent.qqnt.protocol

import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import kotlinx.serialization.json.JsonArray
import moe.fuqiuluo.xposed.helper.LogCenter

internal object MsgSvc: BaseSvc() {
    suspend fun sendToAIO(chatType: Int, peedId: String, message: JsonArray): Pair<Long, Int> {
        val callback = MessageCallback(peedId, 0)
        val result = MessageHelper.sendMessageWithoutMsgId(chatType, peedId, message, callback)
        callback.hashCode = result.second
        return result
    }

    private class MessageCallback(
        private val peerId: String,
        var hashCode: Int
    ): IOperateCallback {
        override fun onResult(code: Int, reason: String?) {
            if (code != 0 && hashCode != 0) {
                MessageHelper.removeMsgByHashCode(hashCode)
            }
            LogCenter.log("消息发送: $peerId, code: $code $reason")
        }
    }
}