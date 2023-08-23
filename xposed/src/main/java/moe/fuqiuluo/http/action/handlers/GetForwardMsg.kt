package moe.fuqiuluo.http.action.handlers

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MultiForwardMsgElement
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.NTServiceFetcher

internal object GetForwardMsg: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val id = session.getStringOrNull("id") ?: return noParam("id")

        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val msgService = sessionService.msgService

        val contact = MessageHelper.generateContact(MsgConstant.KCHATTYPEGROUP, "645830205")
        val msgId = MessageHelper.createMessageUniseq(MsgConstant.KCHATTYPEGROUP, System.currentTimeMillis())
        msgService.addLocalRecordMsg(contact, msgId, MsgElement().apply {
            elementType = MsgConstant.KELEMTYPEMULTIFORWARD
            multiForwardMsgElement = MultiForwardMsgElement("", id, "")
        }, hashMapOf(), true) { code, why ->
            LogCenter.log("GetForwardMsg addLocalRecordMsg code: $code, why: $why, $msgId")
        }

        msgService.fetchLongMsg(contact, msgId)


        return ""
    }
    override fun path(): String  = "get_forward_msg"
}