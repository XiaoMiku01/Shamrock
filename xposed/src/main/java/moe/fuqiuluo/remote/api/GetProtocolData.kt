package moe.fuqiuluo.remote.api

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.dt.model.FEBound
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.protocol.TicketSvc
import io.ktor.server.routing.Routing
import moe.fuqiuluo.remote.entries.Protocol
import moe.fuqiuluo.remote.entries.QSignDtConfig
import moe.fuqiuluo.remote.entries.Status
import moe.fuqiuluo.xposed.tools.fetchOrThrow
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import moe.fuqiuluo.xposed.tools.respond
import moe.fuqiuluo.xposed.tools.toHexString
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tlv_type.tlv_t100
import oicq.wlogin_sdk.tlv_type.tlv_t106
import oicq.wlogin_sdk.tlv_type.tlv_t18
import oicq.wlogin_sdk.tools.util

fun Routing.obtainProtocolData() {
    getOrPost("/send_packet") {
        val uin = fetchOrThrow("uin")
        val cmd = fetchOrThrow("cmd")
        val isPb = fetchOrThrow("proto").toBooleanStrict()
        val buffer = fetchOrThrow("buffer").hex2ByteArray()
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val toServiceMsg = ToServiceMsg("mobileqq.service", uin, cmd)
        toServiceMsg.putWupBuffer(buffer)
        toServiceMsg.addAttribute("req_pb_protocol_flag", isPb)
        app.sendToService(toServiceMsg)
        respond(true, Status.Ok)
    }

    getOrPost("/get_ticket") {
        val uin = fetchOrThrow("uin")
        val ticket = when(fetchOrThrow("id").toInt()) {
            32 -> TicketSvc.getStWeb(uin)
            else -> error("不支持获取该Ticket")
        }
        respond(true, Status.Ok, "success", ticket)
    }

    getOrPost("/get_msf_info") {
        val mqq = MobileQQ.getMobileQQ()
        val ctx = MobileQQ.getContext()

        val t18 = tlv_t18()
        val t100 = tlv_t100()
        val t106 = tlv_t106()

        val qimei = kotlin.runCatching {
            moe.fuqiuluo.xposed.tools.util.buf_to_string(util.get_qimei(ctx))
        }.getOrNull()

        val encodeTable = FEBound::class.java.getDeclaredField("mConfigEnCode").also {
            it.isAccessible = true
        }.get(null) as Array<ByteArray>
        val decodeTable = FEBound::class.java.getDeclaredField("mConfigDeCode").also {
            it.isAccessible = true
        }.get(null) as Array<ByteArray>

        respond(
            isOk = true,
            code = Status.Ok,
            data = Protocol(
                mqq.qqProcessName,
                mqq.appId.toLong(), mqq.qua, mqq.ntCoreVersion,
                mqq.msfConnectedNetType,
                qimei ?: "",
                util.getSvnVersion(),
                moe.fuqiuluo.xposed.tools.util.buf_to_string( util.getGuidFromFile(ctx) ),
                moe.fuqiuluo.xposed.tools.util.buf_to_string( util.get_ksid(ctx) ),
                util.get_network_type(ctx),
                t18._ping_version.toByte(), t18._sso_version,
                t100._sso_ver, t100._db_buf_ver,
                t106._SSoVer, t106._TGTGTVer,

                util.get_android_dev_info(ctx).toHexString(),

                qSignDtConfig = QSignDtConfig(encodeTable, decodeTable)
            )
        )
    }
}