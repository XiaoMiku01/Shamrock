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

        return error("不支持实现，请提交ISSUE！")
    }
    override fun path(): String  = "get_forward_msg"
}