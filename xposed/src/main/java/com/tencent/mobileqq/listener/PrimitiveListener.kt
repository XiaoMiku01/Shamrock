package com.tencent.mobileqq.listener

import com.tencent.qqnt.helper.ContactHelper
import com.tencent.qqnt.protocol.GroupSvc
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asLong
import moe.fuqiuluo.proto.asUtf8String
import moe.fuqiuluo.utils.ServiceUtils
import moe.fuqiuluo.xposed.helper.Level
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
        val msgTime = pb[1, 2, 6].asInt
        when(msgType) {
            732 -> onGroupBan(msgTime, pb)
        }
    }

    private suspend fun onGroupBan(msgTime: Int, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        val operatorUid = pb[1, 3, 2, 4].asUtf8String
        val targetUid = pb[1, 3, 2, 5, 3, 1].asUtf8String
        val duration = pb[1, 3, 2, 5, 3, 2].asInt

        val operation = ContactHelper.getUinByUidAsync(operatorUid)
        val target = ContactHelper.getUinByUidAsync(targetUid)


    }
}