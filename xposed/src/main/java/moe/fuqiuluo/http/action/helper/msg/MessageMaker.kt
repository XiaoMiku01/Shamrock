@file:Suppress("UNUSED_PARAMETER", "RedundantSuspendModifier")
package moe.fuqiuluo.http.action.helper.msg

import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.emoticon.QQSysFaceUtil
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.fuqiuluo.http.action.helper.ContactHelper
import moe.fuqiuluo.http.action.helper.FileHelper
import moe.fuqiuluo.http.action.helper.HighwayHelper
import moe.fuqiuluo.http.action.helper.LocationHelper
import moe.fuqiuluo.http.action.helper.MusicHelper
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.http.action.helper.TroopHelper
import moe.fuqiuluo.http.action.helper.codec.AudioUtils
import moe.fuqiuluo.http.action.helper.codec.MediaType
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import moe.fuqiuluo.xposed.helper.msgService
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asIntOrNull
import moe.fuqiuluo.xposed.tools.asLong
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty
import mqq.app.MobileQQ
import tencent.im.oidb.cmd0xb77.oidb_cmd0xb77
import tencent.im.oidb.cmd0xdc2.oidb_cmd0xdc2
import tencent.im.oidb.oidb_sso
import java.io.File
import kotlin.math.roundToInt

internal typealias IMaker = suspend (Int, Long, String, JsonObject) -> MsgElement

internal object MessageMaker {
    private val makerArray = mutableMapOf(
        "text" to ::createTextElem,
        "face" to ::createFaceElem,
        "pic" to ::createImageElem,
        "image" to ::createImageElem,
        "record" to ::createRecordElem,
        "at" to ::createAtElem,
        "video" to ::createVideoElem,
        "markdown" to ::createMarkdownElem,
        "dice" to ::createDiceElem,
        "rps" to ::createRpsElem,
        "poke" to ::createPokeElem,
        "anonymous" to ::createAnonymousElem,
        "share" to ::createShareElem,
        "contact" to ::createContactElem,
        "location" to ::createLocationElem,
        "music" to ::createMusicElem,
        "reply" to ::createReplyElem,
    )

    private suspend fun createReplyElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("id")
        val element = MsgElement()
        element.elementType = MsgConstant.KELEMTYPEREPLY
        val reply = ReplyElement()
        reply.replayMsgId = data["id"].asString.toLong()
        if(data.containsKey("text")) {
            data.checkAndThrow("qq", "time", "seq")
            reply.replayMsgSeq = data["seq"].asLong
            reply.sourceMsgText = data["text"].asString
            reply.replyMsgTime = data["time"].asLong
            reply.senderUid = data["qq"].asString.toLong()
        }
        element.replyElement = reply
        return element
    }

    private suspend fun createMusicElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("type")

        when(data["type"].asString) {
            "qq" -> {
                data.checkAndThrow("id")
                val id = data["id"].asString
                if(!MusicHelper.trySendQQMusicInfoById(chatType, peerId.toLong(), msgId, id)) {
                    throw LogicException("无法发送QQ音乐分享")
                }
            }
            "163" -> {
                data.checkAndThrow("id")
                val id = data["id"].asString
                if(!MusicHelper.trySend163MusicInfoById(chatType, peerId.toLong(), msgId, id)) {
                    throw LogicException("无法发送网易云音乐分享")
                }
            }
            "custom" -> {
                data.checkAndThrow("url", "audio", "title")
                MusicHelper.trySendMusicShareCustom(
                    chatType,
                    peerId.toLong(),
                    msgId,
                    data["title"].asString,
                    data["singer"].asStringOrNull ?: "",
                    data["url"].asString,
                    "",
                    data["url"].asString
                )
            }
            else -> error("不支持该类型音乐发送")
        }

        return MsgElement().also {
            it.elementType = -1
        }
    }

    private suspend fun createLocationElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("lat", "lon")

        val lat = data["lat"].asString.toDouble()
        val lon = data["lon"].asString.toDouble()

        LocationHelper.sendShareLocation(chatType, peerId.toLong(), lat, lon)

        return MsgElement().also {
            it.elementType = -1
        }
    }

    private suspend fun createContactElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("type", "id")
        val type = data["type"].asString
        val id = data["id"].asString
        val elem = MsgElement()

        when (type) {
            "qq" -> {
                val ark = ArkElement(ContactHelper.getSharePrivateContact(id.toLong()), null, null)
                elem.arkElement = ark
            }
            "group" -> {
                val ark = ArkElement(ContactHelper.getShareTroopContact(id.toLong()), null, null)
                elem.arkElement = ark
            }
            else -> throw ParamsIllegalException("type")
        }

        elem.elementType = MsgConstant.KELEMTYPEARKSTRUCT
        return elem
    }

    private suspend fun createShareElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("title", "url")

        val url = data["url"].asString
        val image = if (data.containsKey("image")) {
            data["image"].asString
        } else {
            val startWithPrefix = url.startsWith("http://") || url.startsWith("https://")
            val endWithPrefix = url.startsWith("/")
            "http://" + url.split("/")[if (startWithPrefix) 2 else 0] + if (!endWithPrefix) {
                "/favicon.ico"
            } else {
                "favicon.ico"
            }
        }
        val title = data["title"].asString
        val content = data["content"].asStringOrNull

        val reqBody = oidb_cmd0xdc2.ReqBody()
        val info = oidb_cmd0xb77.ReqBody()
        info.appid.set(100446242L)
        info.app_type.set(1)
        info.msg_style.set(0)
        info.recv_uin.set(peerId.toLong())
        val clientInfo = oidb_cmd0xb77.ClientInfo()
        clientInfo.platform.set(1)
        info.client_info.set(clientInfo)
        val richMsgBody = oidb_cmd0xb77.RichMsgBody()
        richMsgBody.using_ark.set(true)
        richMsgBody.title.set(title)
        // "using_ark", "title", "summary", "brief", "url", "picture_url", "action", "music_url", "image_info"
        richMsgBody.summary.set(content ?: url)
        richMsgBody.brief.set("[分享] $title")
        richMsgBody.url.set(url)
        richMsgBody.picture_url.set(image)
        info.ext_info.set(oidb_cmd0xb77.ExtInfo().also {
            it.msg_seq.set(msgId)
        })
        info.rich_msg_body.set(richMsgBody)
        reqBody.msg_body.set(info)
        val sendTo = oidb_cmd0xdc2.BatchSendReq()
        when (chatType) {
            MsgConstant.KCHATTYPEGROUP -> sendTo.send_type.set(1)
            MsgConstant.KCHATTYPEC2C -> sendTo.send_type.set(0)
            else -> return createTextElem(
                chatType = chatType,
                msgId = msgId,
                peerId = peerId,
                data = JsonObject(mapOf("text" to JsonPrimitive("[分享] $title\n地址: $url")))
            )
        }
        sendTo.recv_uin.set(peerId.toLong())
        reqBody.batch_send_req.add(sendTo)
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val to = ToServiceMsg("mobileqq.service", app.currentAccountUin, "OidbSvc.0xdc2_34")
        val oidb = oidb_sso.OIDBSSOPkg()
        oidb.uint32_command.set(0xdc2)
        oidb.uint32_service_type.set(34)
        oidb.bytes_bodybuffer.set(ByteStringMicro.copyFrom(reqBody.toByteArray()))
        oidb.str_client_version.set(PlatformHelper.getClientVersion(MobileQQ.getContext()))
        to.putWupBuffer(oidb.toByteArray())
        to.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(to)
        return MsgElement().also {
            it.elementType = -1
        }
    }

    private suspend fun createAnonymousElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        return MsgElement().also {
            it.elementType = -1
        }
    }

    private suspend fun createPokeElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("type", "id")
        val elem = MsgElement()
        val face = FaceElement()
        face.faceIndex = 0
        face.faceText = ""
        face.faceType = 5
        face.packId = null
        face.pokeType = data["type"].asInt
        face.spokeSummary = ""
        face.doubleHit = 0
        face.vaspokeId = data["id"].asInt
        face.vaspokeName = ""
        face.vaspokeMinver = ""
        face.pokeStrength = (data["strength"].asIntOrNull ?: data["cnt"].asIntOrNull
                ?: data["count"].asIntOrNull ?: data["time"].asIntOrNull ?: 0).also {
            if(it < 0 || it > 3) throw ParamsIllegalException("strength")
        }
        face.msgType = 0
        face.faceBubbleCount = 0
        face.oldVersionStr = "[截一戳]请使用最新版手机QQ体验新功能。"
        face.pokeFlag = 0
        elem.elementType = MsgConstant.KELEMTYPEFACE
        elem.faceElement = face
        return elem
    }

    private suspend fun createFaceElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("id")

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEFACE
        val face = FaceElement()

        // 4 is market face
        // 5 is vas poke
        face.faceType = 0
        val serverId =  data["id"].asInt
        val localId = QQSysFaceUtil.convertToLocal(serverId)
        face.faceIndex = serverId
        face.faceText = QQSysFaceUtil.getFaceDescription(localId)
        face.imageType = 0
        face.packId = "0"
        elem.faceElement = face

        return elem
    }

    private suspend fun createRpsElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEMARKETFACE
        val market = MarketFaceElement(
            6, 1, 11415, 3, 0, 200, 200,
            "[猜拳]", "83C8A293AE65CA140F348120A77448EE", "7de39febcf45e6db",
            null, null, 0, 0, 0, 1, 0,
            null, null, null,
            "", null, null,
            null, null, arrayListOf(MarketFaceSupportSize(200, 200)), null)
        elem.marketFaceElement = market
        return elem
    }

    private suspend fun createDiceElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEMARKETFACE
        val market = MarketFaceElement(
            6, 1, 11464, 3, 0, 200, 200,
            "[骰子]", "4823d3adb15df08014ce5d6796b76ee1", "409e2a69b16918f9",
            null, null, 0, 0, 0, 1, 0,
            null, null, null, // jumpurl
            "", null, null,
            null, null, arrayListOf(MarketFaceSupportSize(200, 200)), null)
        elem.marketFaceElement = market
        return elem
    }

    private suspend fun createMarkdownElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        if (chatType != MsgConstant.KCHATTYPEGUILD) {
            return createTextElem(chatType, msgId, peerId, data)
        }
        data.checkAndThrow("text")
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEMARKDOWN
        val markdown = MarkdownElement(data["text"].asString)
        elem.markdownElement = markdown
        return elem
    }

    private suspend fun createVideoElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        val file = FileHelper.parseAndSave(data["file"].asString)
        val elem = MsgElement()
        val video = VideoElement()

        video.videoMd5 = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        val msgService = NTServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
            5, 2, video.videoMd5, file.name, 1, 0, null, "", true
        ))
        val thumbPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
            5, 1, video.videoMd5, file.name, 2, 0, null, "", true
        ))
        if (!QQNTWrapperUtil.CppProxy.fileIsExist(originalPath) || QQNTWrapperUtil.CppProxy.getFileSize(originalPath) != file.length()) {
            QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, originalPath)
            AudioUtils.obtainVideoCover(file.absolutePath, thumbPath!!)
        }

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopVideo(peerId, file, File(thumbPath!!))
        } else if (chatType == MsgConstant.KCHATTYPEC2C) {
            HighwayHelper.transC2CVideo(peerId, file, File(thumbPath!!))
        }

        video.fileTime = AudioUtils.getVideoTime(file)
        video.fileSize = file.length()
        video.fileName = file.name
        video.fileFormat = HighwayHelper.VIDEO_FORMAT_MP4
        video.thumbSize = QQNTWrapperUtil.CppProxy.getFileSize(thumbPath).toInt()
        val options = BitmapFactory.Options()
        BitmapFactory.decodeFile(thumbPath, options)
        video.thumbWidth = options.outWidth
        video.thumbHeight = options.outHeight
        video.thumbMd5 = QQNTWrapperUtil.CppProxy.genFileMd5Hex(thumbPath)
        video.thumbPath = hashMapOf(0 to thumbPath)

        elem.videoElement = video
        elem.elementType = MsgConstant.KELEMTYPEVIDEO
        return elem
    }

    private suspend fun createAtElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        if (chatType != MsgConstant.KCHATTYPEGROUP) {
            return MsgElement()
        }
        data.checkAndThrow("qq")

        val elem = MsgElement()
        val qq = data["qq"].asString

        val at = TextElement()
        when(qq) {
            "0", "all" -> {
                at.content = "@全体成员"
                at.atType = MsgConstant.ATTYPEALL
                at.atNtUid = "0"
            }
            "online" -> {
                at.content = "@在线成员"
                at.atType = MsgConstant.ATTYPEONLINE
                at.atNtUid = "0"
            }
            "admin" -> {
                at.content = "@管理员"
                at.atRoleId = 1
                at.atType = MsgConstant.ATTYPEROLE
                at.atNtUid = "0"
            }
            else -> {
                val info = TroopHelper.getTroopMemberInfoByUin(peerId, qq.toLong()) ?: error("获取成员昵称失败")
                at.content = "@${info.cardName
                    .ifNullOrEmpty(info.nick)
                    .ifNullOrEmpty(qq)}"
                at.atType = MsgConstant.ATTYPEONE
                at.atNtUid = info.uid
            }
        }

        elem.textElement = at
        elem.elementType = MsgConstant.KELEMTYPETEXT
        return elem
    }

    private suspend fun createRecordElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        var file = FileHelper.parseAndSave(data["file"].asString)
        val isMagic = data["magic"].asStringOrNull == "1"

        val ptt = PttElement()

        when (AudioUtils.getMediaType(file)) {
            MediaType.Silk -> {
                ptt.formatType = MsgConstant.KPTTFORMATTYPESILK
                ptt.duration = 1
            }
            MediaType.Amr -> {
                ptt.duration = AudioUtils.getDurationSec(file)
                ptt.formatType = MsgConstant.KPTTFORMATTYPEAMR
            }
            MediaType.Pcm -> {
                val result = AudioUtils.pcmToSilk(file)
                ptt.duration = (result.second * 0.001).roundToInt()
                file = result.first
                ptt.formatType = MsgConstant.KPTTFORMATTYPESILK
            }
            else -> {
                val result = AudioUtils.audioToSilk(file)
                ptt.duration = result.first
                file = result.second
                ptt.formatType = MsgConstant.KPTTFORMATTYPESILK
            }
        }
        val msgService = NTServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
            MsgConstant.KELEMTYPEPTT, 0, ptt.md5HexStr, file.name, 1, 0, null, "", true
        ))!!
        if (!QQNTWrapperUtil.CppProxy.fileIsExist(originalPath) || QQNTWrapperUtil.CppProxy.getFileSize(originalPath) != file.length()) {
            QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, originalPath)
        }

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopVoice(peerId, file)
        } else if (chatType == MsgConstant.KCHATTYPEC2C) {
            HighwayHelper.transC2CVoice(peerId, file)
        }

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEPTT
        ptt.md5HexStr = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        ptt.fileName = file.name
        ptt.filePath = file.absolutePath
        ptt.fileSize = file.length()

        if (!isMagic) {
            ptt.voiceType = MsgConstant.KPTTVOICETYPESOUNDRECORD
            ptt.voiceChangeType = MsgConstant.KPTTVOICECHANGETYPENONE
        } else {
            ptt.voiceType = MsgConstant.KPTTVOICETYPEVOICECHANGE
            ptt.voiceChangeType = MsgConstant.KPTTVOICECHANGETYPEECHO
        }

        ptt.canConvert2Text = false
        ptt.fileId = 0
        ptt.fileUuid = ""
        ptt.text = ""

        elem.pttElement = ptt
        return elem
    }

    private suspend fun createImageElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        val isOriginal = data["original"].asBooleanOrNull ?: true
        val isFlash = data["flash"].asBooleanOrNull ?: false
        val file = FileHelper.parseAndSave(data["file"].asString)

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopPic(peerId, file)
        } else if (chatType == MsgConstant.KCHATTYPEC2C) {
            HighwayHelper.transC2CPic(peerId, file)
        }

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEPIC
        val pic = PicElement()
        pic.md5HexStr = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        val msgService = NTServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
            2, 0, pic.md5HexStr, file.name, 1, 0, null, "", true
        ))
        if (!QQNTWrapperUtil.CppProxy.fileIsExist(originalPath) || QQNTWrapperUtil.CppProxy.getFileSize(originalPath) != file.length()) {
            val thumbPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
                2, 0, pic.md5HexStr, file.name, 2, 720, null, "", true
            ))
            QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, originalPath)
            QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, thumbPath)
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        val exifInterface = ExifInterface(file.absolutePath)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        if (orientation != ExifInterface.ORIENTATION_ROTATE_90 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
            pic.picWidth = options.outWidth
            pic.picHeight = options.outHeight
        } else {
            pic.picWidth = options.outHeight
            pic.picHeight = options.outWidth
        }
        pic.sourcePath = file.absolutePath
        pic.fileSize = QQNTWrapperUtil.CppProxy.getFileSize(file.absolutePath)
        pic.original = isOriginal
        pic.picType = FileHelper.getPicType(file)
        // GO-CQHTTP扩展参数 支持
        pic.picSubType = data["subType"].asIntOrNull ?: 0
        pic.isFlashPic = isFlash

        elem.picElement = pic
        return elem
    }

    private suspend fun createTextElem(chatType: Int, msgId: Long, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("text")
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPETEXT
        val text = TextElement()
        text.content = data["text"].asString
        elem.textElement = text
        return elem
    }

    private fun JsonObject.checkAndThrow(vararg key: String) {
        key.forEach {
            if (!containsKey(it)) throw ParamsException(it)
        }
    }

    operator fun get(type: String): IMaker? = makerArray[type]
}