@file:OptIn(DelicateCoroutinesApi::class)

package moe.fuqiuluo.http.action.helper

import android.util.LruCache
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.protofile.join_group_link.join_group_link
import com.tencent.qphone.base.remote.ToServiceMsg
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.xposed.helper.DynamicReceiver
import moe.fuqiuluo.xposed.helper.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import moe.fuqiuluo.xposed.tools.slice
import mqq.app.MobileQQ
import tencent.im.oidb.cmd0x11b2.oidb_0x11b2
import tencent.im.oidb.oidb_sso

internal object ContactHelper {
    private val LruCachePrivate = LruCache<Long, String>(5)
    private val LruCacheTroop = LruCache<Long, String>(5)

    init {
        MobileQQ.getContext().broadcast("msf") {
            putExtra("cmd", "register_handler_cmd")
            putExtra("handler_cmd", "OidbSvcTrpcTcp.0x11ca_0")
        }
        MobileQQ.getContext().broadcast("msf") {
            putExtra("cmd", "register_handler_cmd")
            putExtra("handler_cmd", "GroupSvc.JoinGroupLink")
        }
        val privatePattern = Regex("uin=([0-9]+)\"")
        DynamicReceiver.register("OidbSvcTrpcTcp.0x11ca_0", IPCRequest {
            val body = oidb_sso.OIDBSSOPkg()
            body.mergeFrom(it.getByteArrayExtra("buffer")!!.slice(4))
            val rsp = oidb_0x11b2.BusinessCardV3Rsp()
            rsp.mergeFrom(body.bytes_bodybuffer.get().toByteArray())
            val text = rsp.signed_ark_msg.get()
            val matcher = privatePattern.findAll(text)
            val id = matcher.first().groups[1]!!.value
            LruCachePrivate.put(id.toLong(), text)
        })
        DynamicReceiver.register("GroupSvc.JoinGroupLink", IPCRequest {
            val body = join_group_link.RspBody()
            body.mergeFrom(it.getByteArrayExtra("buffer")!!.slice(4))
            val text = body.signed_ark.get().toStringUtf8()
            val groupId = body.group_code.get()
            LruCacheTroop.put(groupId, text)
        })
    }

    suspend fun getSharePrivateContact(peerId: Long): String {
        LruCachePrivate[peerId]?.let { return it }

        val reqBody = oidb_0x11b2.BusinessCardV3Req()
        reqBody.uin.set(peerId)
        reqBody.jump_url.set("mqqapi://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=$peerId")
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val to = ToServiceMsg("mobileqq.service", app.currentAccountUin, "OidbSvcTrpcTcp.0x11ca_0")
        val oidb = oidb_sso.OIDBSSOPkg()
        oidb.uint32_command.set(4790)
        oidb.uint32_service_type.set(0)
        oidb.bytes_bodybuffer.set(ByteStringMicro.copyFrom(reqBody.toByteArray()))
        oidb.str_client_version.set(PlatformHelper.getClientVersion(MobileQQ.getContext()))
        to.putWupBuffer(oidb.toByteArray())
        to.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(to)

        return withTimeoutOrNull(10000) {
            var text: String? = null
            while (text == null) {
                delay(100)
                LruCachePrivate[peerId]?.let { text = it }
            }
            return@withTimeoutOrNull text
        } ?: error("unable to fetch contact ark_json_text")
    }

    suspend fun getShareTroopContact(peerId: Long): String {
        LruCacheTroop[peerId]?.let { return it }

        val reqBody = join_group_link.ReqBody()
        reqBody.get_ark.set(true)
        reqBody.type.set(1)
        reqBody.group_code.set(peerId)

        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val toServiceMsg = ToServiceMsg("mobileqq.service", app.currentAccountUin, "GroupSvc.JoinGroupLink")
        toServiceMsg.putWupBuffer(reqBody.toByteArray())
        toServiceMsg.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(toServiceMsg)

        return withTimeoutOrNull(10000) {
            var text: String? = null
            while (text == null) {
                delay(100)
                LruCacheTroop[peerId]?.let { text = it }
            }
            return@withTimeoutOrNull text
        } ?: error("unable to fetch contact ark_json_text")
    }
}