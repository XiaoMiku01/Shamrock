package com.tencent.qqnt.utils

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Base64
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyTo
import moe.fuqiuluo.utils.DownloadUtils
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import mqq.app.MobileQQ
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.experimental.and

internal object FileUtils {
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

    suspend fun parseAndSave(file: String): File {
        return if (file.startsWith("base64://")) {
            saveFileToCache(ByteArrayInputStream(
                Base64.decode(file.substring(9), Base64.DEFAULT)
            ))
        } else if (file.startsWith("file:///")) {
            File(file.substring(8)).inputStream().use {
                saveFileToCache( it )
            }
        } else {
            kotlin.run {
                val tmp = getTmpFile()
                DownloadUtils.download(file, tmp)
                tmp.inputStream().use {
                    saveFileToCache(it)
                }.also {
                    tmp.delete()
                    LogCenter.log("文件下载完成: ${it.absolutePath}, 地址: $file", Level.DEBUG)
                }
            }
        }
    }

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

    fun getFile(dir: String, name: String?): File {
        if (name == null) return getFile(dir)
        val file = CacheDir.resolve(dir)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.resolve(name)
    }

    fun getFile(name: String) = CacheDir.resolve(name)

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

    fun getFileType(file: File): String {
        val bytes = ByteArray(2)
        file.inputStream().use {
            it.read(bytes)
        }
        return when ("${bytes[0].toUByte()}${bytes[1].toUByte()}".toInt()) {
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
        return sourceFile
    }

    fun saveFileToCache(input: InputStream): File {
        val tmpFile = getTmpFile()
        tmpFile.outputStream().use {
            input.copyTo(it)
        }
        val md5Hex = QQNTWrapperUtil.CppProxy.genFileMd5Hex(tmpFile.absolutePath)
        val sourceFile = CacheDir.resolve(md5Hex)
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
        tmpFile.renameTo(sourceFile)
        return sourceFile
    }
}