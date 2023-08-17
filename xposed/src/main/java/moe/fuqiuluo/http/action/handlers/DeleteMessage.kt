package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.http.entries.EmptyObject
import moe.fuqiuluo.xposed.helper.MMKVFetcher
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import kotlin.coroutines.resume

internal object DeleteMessage: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val msgId = (session.getStringOrNull("message_id")
            ?: return noParam("message_id")).toLong()

        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val msgService = sessionService.msgService

        val result = suspendCancellableCoroutine { continuation ->
            msgService.recallMsg(generateContact(msgId), arrayListOf(msgId)) { code, why ->
                continuation.resume(code to why)
            }
        }
        return if (result.first == 0) {
            ok(EmptyObject)
        } else {
            logic(result.second)
        }
    }

    private fun generateContact(msgId: Long): Contact {
        val chatType = MessageHelper.getChatType(msgId)
        val mmkv = MMKVFetcher.defaultMMKV()
        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            val key = "troop_$msgId"
            val groupId = mmkv.getLong(key, 0)
            mmkv.remove(key)
            return MessageHelper.generateContact(chatType, groupId.toString())
        }
        kotlin.error("暂时不支持撤回该类型消息: $chatType")
    }

    override fun path(): String = "delete_message"
}