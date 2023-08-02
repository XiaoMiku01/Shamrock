package moe.fuqiuluo.http.api

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.Protocol
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tlv_type.tlv_t100
import oicq.wlogin_sdk.tlv_type.tlv_t106
import oicq.wlogin_sdk.tlv_type.tlv_t18
import oicq.wlogin_sdk.tools.util
import moe.fuqiuluo.xposed.tools.util.buf_to_string

fun Routing.getMsfInfo() {
    get("/get_msf_info") {
        val mqq = MobileQQ.getMobileQQ()
        val ctx = MobileQQ.getContext()

        val t18 = tlv_t18()
        val t100 = tlv_t100()
        val t106 = tlv_t106()

        call.respond(CommonResult("ok", 0, Protocol(
            mqq.qqProcessName,
            mqq.appId.toLong(), mqq.qua, mqq.ntCoreVersion,
            mqq.msfConnectedNetType,
            buf_to_string( util.get_qimei(ctx) ),
            util.getSvnVersion(),
            buf_to_string( util.getGuidFromFile(ctx) ),
            buf_to_string( util.get_ksid(ctx) ),
            util.get_network_type(ctx),

            t18._ping_version.toByte(), t18._sso_version,

            t100._sso_ver, t100._db_buf_ver,

            t106._SSoVer, t106._TGTGTVer
        )))
    }
}