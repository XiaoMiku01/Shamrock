package moe.protocol.service.listener

import moe.protocol.service.HttpService
import moe.protocol.servlet.helper.ContactHelper
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asLong
import moe.fuqiuluo.proto.asUtf8String
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.utils.ServiceUtils
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.PacketHandler
import moe.fuqiuluo.xposed.tools.slice
import mqq.app.MobileQQ

internal object PrimitiveListener {
    private val isInit = atomic(false)

    fun listenTo() {
        if (isInit.value) return
        if (!ServiceUtils.isServiceExisted(
            ctx = MobileQQ.getContext(),
            process = "com.tencent.mobileqq:MSF"
        ) || !PacketHandler.isInit) {
            return
        }

        PacketHandler.register("trpc.msg.olpush.OlPushService.MsgPush") {
            GlobalScope.launch {
                onMsgPush(ProtoUtils.decodeFromByteArray(it.slice(4)))
            }
        }

        isInit.value = true
    }

    private suspend fun onMsgPush(pb: ProtoMap) {
        val msgType = pb[1, 2, 1].asInt
        val msgTime = pb[1, 2, 6].asLong
        when(msgType) {
            34 -> onGroupMemberDecreased(msgTime, pb)
            44 -> onGroupAdminChange(msgTime, pb)
            732 -> onGroupBan(msgTime, pb)
        }
    }

    private suspend fun onGroupMemberDecreased(time: Long, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        val targetUid = pb[1, 3, 2, 3].asUtf8String
        val type = pb[1, 3, 2, 4].asInt // 131 passive | 130 active

        val target = ContactHelper.getUinByUidAsync(targetUid).toLong()
        LogCenter.log("群成员减少($groupCode): $target")


    }

    private suspend fun onGroupAdminChange(msgTime: Long, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        lateinit var targetUid: String
        val isSetAdmin: Boolean
        if (pb.has(1, 3, 2, 4, 1)) {
            targetUid = pb[1, 3, 2, 4, 1, 1].asUtf8String
            isSetAdmin = pb[1, 3, 2, 4, 1, 2].asInt == 1
        } else {
            targetUid = pb[1, 3, 2, 4, 2, 1].asUtf8String
            isSetAdmin = pb[1, 3, 2, 4, 2, 2].asInt == 1
        }
        val target = ContactHelper.getUinByUidAsync(targetUid).toLong()
        LogCenter.log("群管理员变动($groupCode): $target, isSetAdmin = $isSetAdmin")

        HttpService.pushGroupAdminChange(msgTime, target, groupCode, isSetAdmin)
    }

    private suspend fun onGroupBan(msgTime: Long, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        val operatorUid = pb[1, 3, 2, 4].asUtf8String
        val targetUid = pb[1, 3, 2, 5, 3, 1].asUtf8String
        val duration = pb[1, 3, 2, 5, 3, 2].asInt

        val operation = ContactHelper.getUinByUidAsync(operatorUid).toLong()
        val target = ContactHelper.getUinByUidAsync(targetUid).toLong()

        LogCenter.log("群禁言($groupCode): $operation -> $target, 时长 = ${duration}s")

        HttpService.pushGroupBan(msgTime, operation, target, groupCode, duration)
    }
}