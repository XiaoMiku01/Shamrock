package moe.fuqiuluo.http.action.helper

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.http.action.helper.codec.SilkProcessor
import mqq.app.MobileQQ
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.experimental.and


enum class MediaType {
    Mp3,
    Amr,
    M4a,
    Pcm,
    Silk,
    Wav
}

object FileHelper {
    private val CacheDir = MobileQQ.getContext().getExternalFilesDir(null)!!
        .parentFile!!.resolve("Tencent/Shamrock/tmpfiles").also {
            if (it.exists()) it.delete()
            it.mkdirs()
        }
    private val PicIdMap = hashMapOf(
        "jpg" to 1000,
        "bmp" to 1005,
        "gif" to 2000,
        "png" to 1001,
        "webp" to 1002,
        "sharpp" to 1004,
        "apng" to 2001,
    )

    fun isSilk(file: File): Boolean {
        if (file.length() <= 7) {
            return false
        }

        val bytes = ByteArray(7)
        file.inputStream().use {
            it.read(bytes)
        }

        return bytes[1] == 0x23.toByte() && bytes[2] == 0x21.toByte() && bytes[3] == 0x53.toByte()
                && bytes[4] == 0x49.toByte() && bytes[5] == 0x4c.toByte() && bytes[6] == 0x4b.toByte()
    }

    fun getTmpFile(prefix: String = "tmp", create: Boolean = true): File {
        if(!CacheDir.exists()) {
            CacheDir.mkdirs()
        }
        return CacheDir.resolve(prefix + "_" + UUID.randomUUID().toString()).also {
            if (create && !it.exists()) it.createNewFile()
        }
    }

    fun getFile(name: String) = CacheDir.resolve(name)

    fun getMediaType(file: File): MediaType {
        if(isSilk(file)) {
            return MediaType.Silk
        }

        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        var formatMime = ""
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            when(val mime = format.getString(MediaFormat.KEY_MIME)) {
                "audio/mp4a-latm" -> return MediaType.M4a
                "audio/amr-wb", "audio/amr", "audio/3gpp" -> return MediaType.Amr
                "audio/wav" -> return MediaType.Wav
                "audio/mpeg_L2", "audio/mpeg_L1", "audio/mpeg", "audio/mpeg2" -> return MediaType.Mp3
                else -> {
                    if (mime?.startsWith("audio/") == true) formatMime = mime
                }
            }
        }

        extractor.release()
        error("不支持识别的格式：$formatMime")
    }

    fun getAudioMediaMime(file: File): String? {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        var audioFormat: String? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioFormat = mime
                break
            }
        }
        extractor.release()
        return audioFormat
    }

    fun getAudioDuration(audioFilePath: String): Long {
        // 毫秒
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audioFilePath)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationString?.toLong() ?: 0
        retriever.release()
        return duration
    }

    fun getFileType(file: File): String {
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

    fun getPicType(file: File): Int {
        return PicIdMap[getFileType(file)] ?: 1000
    }

    suspend fun saveFileToCache(channel: ByteReadChannel): File {
        val tmpFile = getTmpFile()
        channel.copyTo(tmpFile.writeChannel())
        val md5Hex = QQNTWrapperUtil.CppProxy.genFileMd5Hex(tmpFile.absolutePath)
        val sourceFile = CacheDir.resolve(md5Hex)
        tmpFile.renameTo(sourceFile)
        //input.close() 内存流，无需close
        return sourceFile
    }

    fun saveFileToCache(input: ByteArrayInputStream): File {
        val tmpFile = getTmpFile()
        tmpFile.outputStream().use {
            input.copyTo(it)
        }
        val md5Hex = QQNTWrapperUtil.CppProxy.genFileMd5Hex(tmpFile.absolutePath)
        val sourceFile = CacheDir.resolve(md5Hex)
        tmpFile.renameTo(sourceFile)
        //input.close() 内存流，无需close
        return sourceFile
    }
}