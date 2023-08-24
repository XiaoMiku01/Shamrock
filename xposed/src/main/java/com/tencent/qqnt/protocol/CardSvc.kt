package com.tencent.qqnt.protocol

import android.util.LruCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.xposed.helper.PacketHandler
import moe.fuqiuluo.xposed.tools.slice
import tencent.im.oidb.cmd0x11b2.oidb_0x11b2
import tencent.im.oidb.oidb_sso

internal object CardSvc: BaseSvc() {
    private val LruCachePrivate = LruCache<Long, String>(5)

    init {
        val privatePattern = Regex("uin=([0-9]+)\"")
        PacketHandler.register("OidbSvcTrpcTcp.0x11ca_0") {
            val body = oidb_sso.OIDBSSOPkg()
            body.mergeFrom(it.slice(4))
            val rsp = oidb_0x11b2.BusinessCardV3Rsp()
            rsp.mergeFrom(body.bytes_bodybuffer.get().toByteArray())
            val text = rsp.signed_ark_msg.get()
            val matcher = privatePattern.findAll(text)
            val id = matcher.first().groups[1]!!.value
            LruCachePrivate.put(id.toLong(), text)
        }
    }

    suspend fun getSharePrivateArkMsg(peerId: Long): String {
        LruCachePrivate[peerId]?.let { return it }

        val reqBody = oidb_0x11b2.BusinessCardV3Req()
        reqBody.uin.set(peerId)
        reqBody.jump_url.set("mqqapi://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=$peerId")
        sendOidb("OidbSvcTrpcTcp.0x11ca_0", 4790, 0, reqBody.toByteArray())

        return withTimeoutOrNull(5000) {
            var text: String? = null
            while (text == null) {
                delay(100)
                LruCachePrivate[peerId]?.let { text = it }
            }
            return@withTimeoutOrNull text
        } ?: error("unable to fetch contact ark_json_text")
    }
}