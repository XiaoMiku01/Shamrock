package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.profilecard.api.IProfileProtocolConst
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.protocol.VisitorSvc
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import mqq.app.MobileQQ

internal object SendLike: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        VisitorSvc.vote(session.getLong("user_id"), 1)
        return "成功"
    }

    override fun path(): String = "send_like"
}