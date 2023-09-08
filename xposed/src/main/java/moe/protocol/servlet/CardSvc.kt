@file:OptIn(DelicateCoroutinesApi::class)

package moe.protocol.servlet

import VIP.GetCustomOnlineStatusRsp
import android.util.LruCache
import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.profilecard.api.IProfileDataService
import com.tencent.mobileqq.profilecard.api.IProfileProtocolService
import com.tencent.mobileqq.profilecard.observer.ProfileCardObserver
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import moe.fuqiuluo.xposed.helper.PacketHandler
import moe.fuqiuluo.xposed.tools.GlobalClient
import moe.fuqiuluo.xposed.tools.slice
import mqq.app.MobileQQ
import mqq.app.Packet
import tencent.im.oidb.cmd0x11b2.oidb_0x11b2
import tencent.im.oidb.oidb_sso
import kotlin.coroutines.resume


internal object CardSvc: BaseSvc() {
    private val LruCachePrivate = LruCache<Long, String>(5)
    private val CacheModelShowChannel = Channel<String>()
    private val GetModelShowLock = Mutex()
    private val refreshCardLock = Mutex()

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
        PacketHandler.register("VipCustom.GetCustomOnlineStatus") {
            val rsp = Packet.decodePacket(it, "rsp",  GetCustomOnlineStatusRsp())
            GlobalScope.launch {
                CacheModelShowChannel.send(rsp.sBuffer)
            }
        }
    }

    suspend fun getModelShow(uin: Long = app.longAccountUin): String {
        return GetModelShowLock.withLock {
            val toServiceMsg = createToServiceMsg("VipCustom.GetCustomOnlineStatus")
            toServiceMsg.extraData.putLong("uin", uin)
            send(toServiceMsg)
            withTimeoutOrNull(5000) {
                CacheModelShowChannel.receive()
            } ?: error("unable to fetch contact model_show")
        }
    }

    suspend fun setModelShow(model: String) {
        val pSKey = TicketSvc.getPSKey(app.currentUin)
        val url = "https://club.vip.qq.com/srf-cgi-node?srfname=VIP.CustomOnlineStatusServer.CustomOnlineStatusObj.SetCustomOnlineStatus&ts=${System.currentTimeMillis()}&daid=18&g_tk=${TicketSvc.getCSRF(pSKey)}&pt4_token=${TicketSvc.getPt4Token(pSKey, "club.vip.qq.com") ?: ""}"
        val cookie = TicketSvc.getCookie("club.vip.qq.com")
        GlobalClient.post(url) {
            contentType(Json)
            header("Cookie", cookie)
            setBody("""{
  "servicesName": "VIP.CustomOnlineStatusServer.CustomOnlineStatusObj",
  "cmd": "SetCustomOnlineStatus",
  "args": [
    {
      "sModel": "$model",
      "iAppType": 3,
      "sIMei": "",
      "sVer": "",
      "sManu": "undefined",
      "lUin": ${app.currentUin},
      "bShowInfo": ${model.isNotEmpty()},
      "sModelShow": "$model",
      "bRecoverDefault": ${model.isEmpty()}
    }
  ]
}""")
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

    suspend fun getProfileCard(uin: String): Card {
        return getProfileCardFromCache(uin) ?: refreshAndGetProfileCard(uin)!!
    }

    fun getProfileCardFromCache(uin: String): Card? {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            error("AppRuntime cannot cast to AppInterface")

        val profileDataService = runtime
            .getRuntimeService(IProfileDataService::class.java, "all")
        return profileDataService.getProfileCard(uin, true)
    }

    suspend fun refreshAndGetProfileCard(uin: String): Card? {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is AppInterface)
            error("AppRuntime cannot cast to AppInterface")
        val dataService = app
            .getRuntimeService(IProfileDataService::class.java, "all")
        return refreshCardLock.withLock {
            suspendCancellableCoroutine {
                app.addObserver(object: ProfileCardObserver() {
                    override fun onGetProfileCard(success: Boolean, obj: Any) {
                        app.removeObserver(this)
                        if (!success || obj !is Card) {
                            it.resume(null)
                        } else {
                            dataService.saveProfileCard(obj)
                            it.resume(obj)
                        }
                    }
                })
                app.getRuntimeService(IProfileProtocolService::class.java, "all")
                    .requestProfileCard(app.currentUin, uin, 12, 0L, 0.toByte(), 0L, 0L, null, "", 0L, 10004, null, 0.toByte())
            }
        }
    }

}