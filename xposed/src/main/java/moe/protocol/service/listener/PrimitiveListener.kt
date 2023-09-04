package moe.protocol.service.listener

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import moe.protocol.service.HttpService
import moe.protocol.servlet.helper.ContactHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asLong
import moe.fuqiuluo.proto.asUtf8String
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.proto.asByteArray
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.PacketHandler
import moe.fuqiuluo.xposed.tools.slice
import moe.protocol.service.data.push.NoticeSubType
import moe.protocol.servlet.helper.MessageHelper
import moe.protocol.servlet.protocol.MsgSvc

internal object PrimitiveListener {
    fun registerListener() {
        PacketHandler.register("trpc.msg.olpush.OlPushService.MsgPush") {
            GlobalScope.launch {
                onMsgPush(ProtoUtils.decodeFromByteArray(it.slice(4)))
            }
        }
    }

    private suspend fun onMsgPush(pb: ProtoMap) {
        val msgType = pb[1, 2, 1].asInt
        val msgTime = pb[1, 2, 6].asLong
        when(msgType) {
            33 -> onGroupMemIncreased(msgTime, pb)
            34 -> onGroupMemberDecreased(msgTime, pb)
            44 -> onGroupAdminChange(msgTime, pb)
            732 -> onGroupGrayTip(msgTime, pb)
        }
    }

    private suspend fun onGroupMemIncreased(time: Long, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        val targetUid = pb[1, 3, 2, 3].asUtf8String
        val type = pb[1, 3, 2, 4].asInt
        val operation = ContactHelper.getUinByUidAsync(pb[1, 3, 2, 5].asUtf8String).toLong()
        val target = ContactHelper.getUinByUidAsync(targetUid).toLong()

        LogCenter.log("群成员增加($groupCode): $target, type = $type")

        HttpService.pushGroupMemberDecreased(time, target, groupCode, operation, when(type) {
            130 -> NoticeSubType.Approve
            131 -> NoticeSubType.Invite
            else -> NoticeSubType.Approve
        })
    }

    private suspend fun onGroupMemberDecreased(time: Long, pb: ProtoMap) {
        val groupCode = pb[1, 3, 2, 1].asLong
        val targetUid = pb[1, 3, 2, 3].asUtf8String
        val type = pb[1, 3, 2, 4].asInt
        val operation = ContactHelper.getUinByUidAsync(pb[1, 3, 2, 5].asUtf8String).toLong()
        // 131 passive | 130 active | 3 kick_self

        val target = ContactHelper.getUinByUidAsync(targetUid).toLong()
        LogCenter.log("群成员减少($groupCode): $target, type = $type")

        HttpService.pushGroupMemberDecreased(time, target, groupCode, operation, when(type) {
            130 -> NoticeSubType.Kick
            131 -> NoticeSubType.Leave
            3 -> NoticeSubType.KickMe
            else -> NoticeSubType.Kick
        })
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

    private suspend fun onGroupGrayTip(msgTime: Long, pb: ProtoMap) {
        val groupCode = pb[1, 1, 1].asLong
        val readPacket = ByteReadPacket( pb[1, 3, 2].asByteArray )
        try {
            if (readPacket.readUInt().toLong() == groupCode) {
                val type = readPacket.readUByte()
                val tip = ProtoUtils.decodeFromByteArray(
                    readPacket.readBytes(
                        readPacket.readShort().toInt()
                    )
                )
                onGroupGrayTipV2(msgTime, type, tip)
            } else {
                val operatorUid = pb[1, 3, 2, 4].asUtf8String
                val targetUid = pb[1, 3, 2, 5, 3, 1].asUtf8String
                val duration = pb[1, 3, 2, 5, 3, 2].asInt
                val operation = ContactHelper.getUinByUidAsync(operatorUid).toLong()
                val target = ContactHelper.getUinByUidAsync(targetUid).toLong()
                LogCenter.log("群禁言($groupCode): $operation -> $target, 时长 = ${duration}s")
                HttpService.pushGroupBan(msgTime, operation, target, groupCode, duration)
            }
        } catch (e: Exception) {
            LogCenter.log(e.stackTraceToString(), Level.ERROR)
        } finally {
            readPacket.release()
        }
    }

    private suspend fun onGroupGrayTipV2(time: Long, type: UByte, pb: ProtoMap) {
        when(type.toInt()) {
            17 -> {
                val groupId = pb[4].asLong
                val operatorUid = pb[11, 1].asUtf8String
                val targetUid = pb[11, 3, 6].asUtf8String
                val msgSeq = pb[11, 3, 1].asLong
                val msgTime = pb[11, 3, 2].asLong
                val tip = if (pb.has(11, 9)) pb[11, 9, 2].asUtf8String else ""
                val msgId = MessageHelper.getMsgIdByMsgSeq(MsgConstant.KCHATTYPEGROUP, msgSeq)
                val msgHash = if (msgId == 0L) msgSeq else MessageHelper.generateMsgIdHash(MsgConstant.KCHATTYPEGROUP, msgId)
                val operator = ContactHelper.getUinByUidAsync(operatorUid).toLong()
                val target = ContactHelper.getUinByUidAsync(targetUid).toLong()

                LogCenter.log("群消息撤回($groupId): $operator -> $target, seq = $msgSeq, hash = $msgHash, tip = $tip")


            }
            else -> LogCenter.log("未知的群提示类型: $type", Level.WARN)
        }
    }
}