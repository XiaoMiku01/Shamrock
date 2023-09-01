package moe.fuqiuluo.utils

import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.tencent.qqnt.helper.LocalCacheHelper
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import com.tencent.qqnt.utils.FileUtils
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.loader.NativeLoader
import oicq.wlogin_sdk.tools.MD5
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

enum class MediaType {
    Mp3,
    Amr,
    M4a,
    Pcm,
    Silk,
    Wav,
    Flac,
}

object AudioUtils {
    private val SampleRateMap = intArrayOf(8000, 12000, 16000, 24000, 36000, 44100, 48000)
    private val sampleRate: Int
        get() = SampleRateMap[3]

    init {
        // arrayOf(
        //    "ffmpegkit_abidetect",
        //    "avutil",
        //    "swscale",
        //    "swresample",
        //    "avcodec",
        //    "avformat",
        //    "avfilter",
        //    "avdevice"
        //).forEach {
        //    NativeLoader.load(it)
        //}
        //NativeLoader.load("ffmpegkit")
    }

    fun getVideoTime(file: File): Int {
        val retriever = MediaMetadataRetriever()
        val durationMs: Int = try {
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration!!.toInt()
        } catch (e: Exception) {
            0
        } finally {
            retriever.release()
        }
        return (durationMs * 0.001).roundToInt()
    }

    fun obtainVideoCover(filePath: String, destPath: String) {
        if (destPath.isEmpty()) {
            error("short video thumbs path is empty")
        }
        if (!QQNTWrapperUtil.CppProxy.fileIsExist(destPath)) {
            File(destPath).createNewFile()
        }
        val output = FileOutputStream(destPath)
        output.use {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(filePath)
            val frameAtTime = mediaMetadataRetriever.frameAtTime
            frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 60, it)
            it.flush()
        }
    }

    internal fun audioToSilk(audio: File): Pair<Int, File> {
        val md5 = MD5.getFileMD5(audio)

        val mmkv = MMKVFetcher.mmkvWithId("audio2silk")
        mmkv.getString(md5, null)?.let {
            val silkFile = LocalCacheHelper.getCachePttFile(it)
            if (silkFile.exists()) {
                return getDurationSec(audio) to silkFile
            }
        }

        lateinit var silkFile: File

        val pcmFile = audioToPcm(audio)
        var duration: Int
        pcmToSilk(pcmFile).let {
            val silkMd5 = MD5.getFileMD5(it.first)
            silkFile = LocalCacheHelper.getCachePttFile(silkMd5)
            mmkv.putString(md5, silkMd5)
            it.first.renameTo(silkFile)
            it.first.delete()
            pcmFile.delete()
            duration = it.second
        }
        if (duration < 1000) {
            duration = 1000
        }

        return duration to silkFile
    }

    internal fun pcmToSilk(file: File): Pair<File, Int> {
        val tmpFile = FileUtils.getTmpFile("silk", false)
        val time = pcmToSilk(sampleRate, 2, file.absolutePath, tmpFile.absolutePath)

        val silkMd5 = MD5.getFileMD5(tmpFile)
        val silk = LocalCacheHelper.getCachePttFile(silkMd5)
        tmpFile.renameTo(silk)
        tmpFile.delete()

        when (time) {
            -1 -> error("input pcm file not found")
            -2 -> error("output silk file cannot open")
            -3 -> error("cannot create silk encoder")
            -4 -> error("cannot init silk encoder")
        }
        return silk to time
    }

    fun audioToPcm(audio: File): File {
        try {
            val tmp = FileUtils.getTmpFile("pcm", false)
            //LogCenter.log("PcmTMP: $tmp", Level.DEBUG)
            val ffmpegCommand = "-y -i $audio -f s16le -acodec pcm_s16le -ac 1 -ar $sampleRate $tmp"

            //LogCenter.log("ExecStart: $tmp", Level.DEBUG)

            val session = FFmpegKit.execute(ffmpegCommand)
            if (!ReturnCode.isSuccess(session.returnCode)) {
                error("mp3 to pcm error: ${session.allLogsAsString}")
            }

            //LogCenter.log("KitSession: $session", Level.DEBUG)

            return tmp
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.ERROR)
            throw e
        }
    }

    fun getDurationSec(audio: File): Int {
        return getDuration(audio)
    }

    fun getDuration(audio: File): Int {
        val session = FFprobeKit.getMediaInformation(audio.absolutePath)
        val mediaInformation = session.mediaInformation
        val returnCode: ReturnCode = session.returnCode
        return if (ReturnCode.isSuccess(returnCode) && mediaInformation.duration != null) {
            mediaInformation.duration.split(".")[0].toInt()
        } else {
            1
        }
    }

    fun getMediaType(file: File): MediaType {
        if(FileUtils.isSilk(file)) {
            return MediaType.Silk
        }

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                when (val mime = format.getString(MediaFormat.KEY_MIME)) {
                    "audio/mp4a-latm" -> return MediaType.M4a
                    "audio/amr-wb", "audio/amr", "audio/3gpp" -> return MediaType.Amr
                    "audio/raw", "audio/wav" -> return MediaType.Wav
                    "audio/mpeg_L2", "audio/mpeg_L1", "audio/mpeg", "audio/mpeg2" -> return MediaType.Mp3
                    "audio/flac" -> return MediaType.Flac
                    else -> if (mime?.startsWith("audio/")== true) {
                        LogCenter.log("Unable to check audio: $mime", Level.WARN)
                    }
                }
            }
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.WARN)
        } finally {
            extractor.release()
        }

        return MediaType.Pcm
    }

    private external fun pcmToSilk(rate: Int, type: Byte, pcmFile: String, silkFile: String): Int

    private external fun silkToPcm(rate: Int, type: Byte, pcmFile: String, silkFile: String): Int
}