package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.protocol.MsgSvc
import moe.fuqiuluo.remote.entries.EmptyObject

internal object DeleteMessage: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val hashCode = session.getString("message_id").toInt()
        return invoke(hashCode)
    }

    suspend operator fun invoke(msgHash: Int): String {
        val msgId = MessageHelper.getMsgIdByHashCode(msgHash)
        MsgSvc.recallMsg(msgId)
        return ok(EmptyObject)
    }

    override fun path(): String = "delete_message"

    override val requiredParams: Array<String> = arrayOf("message_id")
}