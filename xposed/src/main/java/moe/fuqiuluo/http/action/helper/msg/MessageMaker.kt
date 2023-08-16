package moe.fuqiuluo.http.action.helper.msg

import android.graphics.BitmapFactory
import android.media.ExifInterface
import com.tencent.mobileqq.emoticon.QQSysFaceUtil
import com.tencent.qqnt.kernel.nativeinterface.FaceElement
import com.tencent.qqnt.kernel.nativeinterface.MarkdownElement
import com.tencent.qqnt.kernel.nativeinterface.MarketFaceElement
import com.tencent.qqnt.kernel.nativeinterface.MarketFaceSupportSize
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.PttElement
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import com.tencent.qqnt.kernel.nativeinterface.RichMediaFilePathInfo
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import com.tencent.qqnt.kernel.nativeinterface.VideoElement
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.helper.FileHelper
import moe.fuqiuluo.http.action.helper.HighwayHelper
import moe.fuqiuluo.http.action.helper.TroopHelper
import moe.fuqiuluo.http.action.helper.codec.AudioUtils
import moe.fuqiuluo.http.action.helper.codec.MediaType
import moe.fuqiuluo.xposed.helper.ServiceFetcher
import moe.fuqiuluo.xposed.helper.msgService
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asIntOrNull
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty
import java.io.File
import kotlin.math.roundToInt

internal typealias IMaker = suspend (Int, String, JsonObject) -> MsgElement

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
        "anonymous" to ::createAnonymousElem
    )

    private suspend fun createAnonymousElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
        // TODO(预计于新版本QQ移除，不予实现)
        return MsgElement()
    }

    private suspend fun createPokeElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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

    private suspend fun createFaceElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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

    private suspend fun createRpsElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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

    private suspend fun createDiceElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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

    private suspend fun createMarkdownElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
        if (chatType != MsgConstant.KCHATTYPEGUILD) {
            return createTextElem(chatType, peerId, data)
        }
        data.checkAndThrow("text")
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEMARKDOWN
        val markdown = MarkdownElement(data["text"].asString)
        elem.markdownElement = markdown
        return elem
    }

    private suspend fun createVideoElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        val file = FileHelper.parseAndSave(data["file"].asString)
        val elem = MsgElement()
        val video = VideoElement()

        video.videoMd5 = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        val msgService = ServiceFetcher.kernelService.msgService!!
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

    private suspend fun createAtElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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

    private suspend fun createRecordElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        var file = FileHelper.parseAndSave(data["file"].asString)
        val isMagic = data["magic"].asBooleanOrNull ?: false

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
        val msgService = ServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(
            MsgConstant.KELEMTYPEPTT, 0, ptt.md5HexStr, file.name, 1, 0, null, "", true
        ))!!
        if (!QQNTWrapperUtil.CppProxy.fileIsExist(originalPath) || QQNTWrapperUtil.CppProxy.getFileSize(originalPath) != file.length()) {
            QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, originalPath)
        }

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopVoice(peerId, file)
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

    private suspend fun createImageElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
        data.checkAndThrow("file")

        val isOriginal = data["original"].asBooleanOrNull ?: true
        val isFlash = data["flash"].asBooleanOrNull ?: false
        val file = FileHelper.parseAndSave(data["file"].asString)

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopPic(peerId, file)
        }

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEPIC
        val pic = PicElement()
        pic.md5HexStr = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        val msgService = ServiceFetcher.kernelService.msgService!!
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
        pic.isFlashPic = isFlash

        elem.picElement = pic
        return elem
    }

    private suspend fun createTextElem(chatType: Int, peerId: String, data: JsonObject): MsgElement {
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