package moe.fuqiuluo.http.action.helper.msg

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Base64
import com.tencent.mobileqq.emoticon.QQSysFaceUtil
import com.tencent.mobileqq.troop.api.ITroopMemberInfoService
import com.tencent.qqnt.kernel.nativeinterface.FaceElement
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.PttElement
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import com.tencent.qqnt.kernel.nativeinterface.RichMediaFilePathInfo
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.helper.FileHelper
import moe.fuqiuluo.http.action.helper.HighwayHelper
import moe.fuqiuluo.http.action.helper.TroopHelper
import moe.fuqiuluo.http.action.helper.codec.AudioUtils
import moe.fuqiuluo.http.action.helper.codec.MediaType
import moe.fuqiuluo.xposed.helper.ServiceFetcher
import moe.fuqiuluo.xposed.helper.msgService
import moe.fuqiuluo.xposed.tools.GlobalClient
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull
import moe.fuqiuluo.xposed.tools.ifNullOrEmpty
import mqq.app.MobileQQ
import java.io.ByteArrayInputStream
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
    )

    private suspend fun createAtElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        if (chatType != MsgConstant.KCHATTYPEGROUP) {
            return MsgElement()
        }
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
                val info = TroopHelper.getTroopMemberInfoByUin(target, qq.toLong()) ?: error("获取成员昵称失败")
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

    private suspend fun createRecordElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        val url = data["file"].asString
        var file = if (url.startsWith("base64://")) {
            FileHelper.saveFileToCache(ByteArrayInputStream(
                Base64.decode(url.substring(9), Base64.DEFAULT)
            ))
        } else if (url.startsWith("file:///")) {
            File(url.substring(8))
        } else {
            kotlin.run {
                val respond = GlobalClient.get(url)
                if (respond.status != HttpStatusCode.OK) {
                    throw Exception("download image failed: ${respond.status}")
                }
                FileHelper.saveFileToCache(respond.bodyAsChannel())
            }
        }
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
            HighwayHelper.transTroopVoice(target, file)
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

    private suspend fun createImageElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        val isOriginal = data["original"].asBooleanOrNull ?: true
        val isFlash = data["flash"].asBooleanOrNull ?: false
        val url = data["file"].asString
        val file = if (url.startsWith("base64://")) {
            FileHelper.saveFileToCache(ByteArrayInputStream(
                Base64.decode(url.substring(9), Base64.DEFAULT)
            ))
        } else if (url.startsWith("file:///")) {
            File(url.substring(8))
        } else {
            kotlin.run {
                val respond = GlobalClient.get(url)
                if (respond.status != HttpStatusCode.OK) {
                    throw Exception("download image failed: ${respond.status}")
                }
                FileHelper.saveFileToCache(respond.bodyAsChannel())
            }
        }

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopPic(target, file)
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

    private suspend fun createFaceElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEFACE
        val face = FaceElement()

        // 4 is market face
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

    private suspend fun createTextElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPETEXT
        val text = TextElement()
        text.content = data["text"].asStringOrNull ?: "null"
        elem.textElement = text
        return elem
    }

    operator fun get(type: String): IMaker? = makerArray[type]
}