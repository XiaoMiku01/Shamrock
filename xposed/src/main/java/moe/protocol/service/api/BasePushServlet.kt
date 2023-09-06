package moe.protocol.service.api

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import moe.protocol.service.data.push.NoticeSubType
import moe.protocol.service.data.push.NoticeType
import mqq.app.MobileQQ

internal interface BasePushServlet {
    val address: String

    fun allowPush(): Boolean

    fun pushPrivateMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int
    )

    fun pushGroupMsg(
        record: MsgRecord,
        elements: List<MsgElement>,
        raw: String,
        msgHash: Int
    )

    fun pushGroupPoke(time: Long, operation: Long, userId: Long, groupId: Long)

    fun pushPrivateMsgRecall(time: Long, operation: Long, msgHash: Long, tip: String)

    fun pushGroupMsgRecall(
        time: Long,
        operation: Long,
        userId: Long,
        groupId: Long,
        msgHash: Long,
        tip: String
    )

    fun pushGroupBan(
        time: Long,
        operation: Long,
        userId: Long,
        groupId: Long,
        duration: Int
    )

    fun pushGroupMemberDecreased(
        time: Long,
        target: Long,
        groupId: Long,
        operation: Long = 0,
        type: NoticeType,
        subType: NoticeSubType
    )

    fun pushGroupAdminChange(time: Long, target: Long, groupId: Long, setAdmin: Boolean)

    val app: QQAppInterface
        get() = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
}