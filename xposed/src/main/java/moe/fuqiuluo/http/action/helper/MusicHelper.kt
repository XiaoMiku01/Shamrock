package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.xposed.tools.GlobalClient
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asJsonArray
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tools.MD5
import tencent.im.oidb.cmd0xb77.oidb_cmd0xb77
import tencent.im.oidb.oidb_sso

internal object MusicHelper {
    suspend fun trySend163MusicInfoById(chatType: Int, peerId: Long, msgId: Long, id: String): Boolean {
        try {
            val respond = GlobalClient.get("https://music.163.com/api/song/detail/?id=$id&ids=[$id]")
            val songInfo = Json.parseToJsonElement(respond.bodyAsText()).asJsonObject["songs"].asJsonArray.first().asJsonObject
            val name = songInfo["name"].asString
            val title = songInfo["name"].asString
            val singerName = songInfo["artists"].asJsonArray.first().asJsonObject["name"].asString
            val previewUrl = songInfo["album"].asJsonObject["picUrl"].asString
            val playUrl = "https://music.163.com/song/media/outer/url?id=$id.mp3"
            val jumpUrl = "https://music.163.com/#/song?id=$id"
            trySendMusicShareCustom(chatType, peerId, msgId, title.ifBlank { name }, singerName, jumpUrl, previewUrl, playUrl)
            return true
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.ERROR)
        }
        return false
    }

    suspend fun trySendQQMusicInfoById(chatType: Int, peerId: Long, msgId: Long, id: String): Boolean {
        try {
            val respond = GlobalClient.get("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0&data={%22comm%22:{%22ct%22:24,%22cv%22:0},%22songinfo%22:{%22method%22:%22get_song_detail_yqq%22,%22param%22:{%22song_type%22:0,%22song_mid%22:%22%22,%22song_id%22:$id},%22module%22:%22music.pf_song_detail_svr%22}}")
            val songInfo = Json.parseToJsonElement(respond.bodyAsText()).asJsonObject["songinfo"].asJsonObject
            if (songInfo["code"].asInt != 0) {
                LogCenter.log("获取QQ音乐($id)的歌曲信息失败。")
                return false
            } else {
                val data = songInfo["data"].asJsonObject
                val trackInfo = data["track_info"].asJsonObject
                val mid = trackInfo["mid"].asString
                val previewMid = trackInfo["album"].asJsonObject["mid"].asString
                val name = trackInfo["name"].asString
                val title = trackInfo["title"].asString
                val singerName = trackInfo["singer"].asJsonArray.first().asJsonObject["name"].asString
                val code = MD5.getMD5String("${mid}q;z(&l~sdf2!nK".toByteArray()).substring(0 .. 4).uppercase()
                val playUrl = "http://c6.y.qq.com/rsc/fcgi-bin/fcg_pyq_play.fcg?songid=&songmid=$mid&songtype=1&fromtag=50&uin=&code=$code"
                val previewUrl = "http://y.gtimg.cn/music/photo_new/T002R180x180M000$previewMid.jpg"
                val jumpUrl = "https://i.y.qq.com/v8/playsong.html?platform=11&appshare=android_qq&appversion=10030010&hosteuin=oKnlNenz7i-s7c**&songmid=${mid}&type=0&appsongtype=1&_wv=1&source=qq&ADTAG=qfshare"
                trySendMusicShareCustom(chatType, peerId, msgId, title.ifBlank { name }, singerName, jumpUrl, previewUrl, playUrl)
                return true
            }
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.ERROR)
        }
        return false
    }

    fun trySendMusicShareCustom(
        chatType: Int,
        peerId: Long,
        msgId: Long,
        title: String,
        singer: String,
        jumpUrl: String,
        previewUrl: String,
        musicUrl: String
    ) {
        val req = oidb_cmd0xb77.ReqBody()
        req.appid.set(100497308)
        req.app_type.set(1)
        req.msg_style.set(4)
        req.client_info.set(oidb_cmd0xb77.ClientInfo().also {
            it.platform.set(1)
            it.sdk_version.set("0.0.0")
            it.android_package_name.set("com.tencent.qqmusic")
            it.android_signature.set("cbd27cd7c861227d013a25b2d10f0799")
        })
        req.ext_info.set(oidb_cmd0xb77.ExtInfo().also {
            it.msg_seq.set(msgId)
        })
        req.recv_uin.set(peerId)
        req.rich_msg_body.set(oidb_cmd0xb77.RichMsgBody().also {
            it.title.set(title)
            it.summary.set(singer)
            it.url.set(jumpUrl)
            it.picture_url.set(previewUrl)
            it.music_url.set(musicUrl)
        })
        when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> req.send_type.set(1)
            MsgConstant.KCHATTYPEC2C -> req.send_type.set(0)
            else -> error("不支持该聊天类型发送音乐分享")
        }
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val toServiceMsg = ToServiceMsg("mobileqq.service", app.currentAccountUin, "OidbSvc.0xb77_9")
        val oidb = oidb_sso.OIDBSSOPkg()
        oidb.uint32_command.set(0xb77)
        oidb.uint32_service_type.set(9)
        oidb.bytes_bodybuffer.set(ByteStringMicro.copyFrom(req.toByteArray()))
        oidb.str_client_version.set(PlatformHelper.getClientVersion(MobileQQ.getContext()))
        toServiceMsg.putWupBuffer(oidb.toByteArray())
        toServiceMsg.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(toServiceMsg)
    }
}