package moe.fuqiuluo.http.api

import com.tencent.mobileqq.dt.model.FEBound
import io.ktor.server.routing.Routing
import moe.fuqiuluo.http.entries.Protocol
import moe.fuqiuluo.http.entries.QSignDtConfig
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.tools.getOrPost
import moe.fuqiuluo.xposed.tools.respond
import moe.fuqiuluo.xposed.tools.toHexString
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tlv_type.tlv_t100
import oicq.wlogin_sdk.tlv_type.tlv_t106
import oicq.wlogin_sdk.tlv_type.tlv_t18
import oicq.wlogin_sdk.tools.util
import moe.fuqiuluo.xposed.tools.util.buf_to_string

fun Routing.getMsfInfo() {
    getOrPost("/get_msf_info") {
        val mqq = MobileQQ.getMobileQQ()
        val ctx = MobileQQ.getContext()

        val t18 = tlv_t18()
        val t100 = tlv_t100()
        val t106 = tlv_t106()

        val qimei = kotlin.runCatching {
            buf_to_string(util.get_qimei(ctx))
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
                buf_to_string( util.getGuidFromFile(ctx) ),
                buf_to_string( util.get_ksid(ctx) ),
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