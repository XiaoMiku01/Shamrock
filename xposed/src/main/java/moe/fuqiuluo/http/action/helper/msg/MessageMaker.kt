package moe.fuqiuluo.http.action.helper.msg

import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.util.Base64
import com.tencent.mobileqq.emoticon.QQSysFaceUtil
import com.tencent.mobileqq.transfile.TransferRequest
import com.tencent.mobileqq.transfile.api.ITransFileController
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.FaceElement
import com.tencent.qqnt.kernel.nativeinterface.IKernelRichMediaService
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import com.tencent.qqnt.kernel.nativeinterface.RichMediaFilePathInfo
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import com.tencent.qqnt.kernel.nativeinterface.UploadGroupFileParams
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.helper.HighwayHelper
import moe.fuqiuluo.xposed.helper.ServiceFetcher
import moe.fuqiuluo.xposed.helper.msgService
import moe.fuqiuluo.xposed.tools.asBooleanOrNull
import moe.fuqiuluo.xposed.tools.asInt
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.asStringOrNull
import mqq.app.MobileQQ
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
import kotlin.experimental.and
import kotlin.random.Random
import kotlin.random.nextLong

internal typealias IMaker = suspend (Int, String, JsonObject) -> MsgElement

internal object MessageMaker {
    private val makerArray = mutableMapOf(
        "text" to ::createTextElem,
        "face" to ::createFaceElem,
        "pic" to ::createImageElem,
        "image" to ::createImageElem,

    )
    private val CacheDir = MobileQQ.getContext().getExternalFilesDir(null)!!
        .parentFile!!.resolve("Tencent/QQ_Images")
    private val PicIdMap = hashMapOf(
        "jpg" to 1000,
        "bmp" to 1005,
        "gif" to 2000,
        "png" to 1001,
        "webp" to 1002,
        "sharpp" to 1004,
        "apng" to 2001,
    )

    private suspend fun createImageElem(chatType: Int, target: String, data: JsonObject): MsgElement {
        val isOriginal = data["original"].asBooleanOrNull ?: true
        val url = data["file"].asString
        lateinit var file: File
        if (url.startsWith("base64://")) {
            file = saveImageToCache(ByteArrayInputStream(
                Base64.decode(url.substring(9), Base64.DEFAULT)
            ))
        } else if (url.startsWith("file:///")) {

        } else {

        }

        if (chatType == MsgConstant.KCHATTYPEGROUP) {
            HighwayHelper.transTroopMessage(target, file)
        }

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEPIC
        val pic = PicElement()
        pic.md5HexStr = QQNTWrapperUtil.CppProxy.genFileMd5Hex(file.absolutePath)

        val msgService = ServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(2, 0, pic.md5HexStr, file.name, 1, 0, null, "", true))
        val thumbPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(2, 0, pic.md5HexStr, file.name, 2, 720, null, "", true))
        QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, originalPath)
        QQNTWrapperUtil.CppProxy.copyFile(file.absolutePath, thumbPath)

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
        pic.picType = PicIdMap[getFileType(file)] ?: 1000

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

    private fun getFileType(file: File): String {
        val bytes = ByteArray(2)
        file.inputStream().use {
            it.read(bytes)
        }
        return when ("${(bytes[0] and 255.toByte())}${(bytes[1] and 255.toByte())}".toInt()) {
            6677 -> "bmp"
            7173 -> "gif"
            7784 -> "midi"
            7790 -> "exe"
            8075 -> "zip"
            8273 -> "webp"
            8297 -> "rar"
            13780 -> "png"
            255216 -> "jpg"
            else -> "jpg"
        }
    }

    private fun saveImageToCache(input: ByteArrayInputStream): File {
        val tmpFile = CacheDir.resolve(UUID.randomUUID().toString())
        tmpFile.outputStream().use {
            input.copyTo(it)
        }
        val md5Hex = QQNTWrapperUtil.CppProxy.genFileMd5Hex(tmpFile.absolutePath)
        val sourceFile = CacheDir.resolve(md5Hex)
        tmpFile.renameTo(sourceFile)
        //input.close() 内存流，无需close
        return sourceFile
    }

    operator fun get(type: String): IMaker? = makerArray[type]
}