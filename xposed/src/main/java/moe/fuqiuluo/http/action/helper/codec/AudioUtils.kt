package moe.fuqiuluo.http.action.helper.codec

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import moe.fuqiuluo.http.action.helper.FileHelper
import java.io.File
internal object AudioUtils {
    internal fun pcmToSilk(file: File): Pair<File, Double> {
        val tmpFile = FileHelper.getTmpFile("silk", false)
        return tmpFile to pcmToSilk(file.absolutePath, tmpFile.absolutePath)
    }

    fun audioToPcm(audio: File): File {
        val tmp = FileHelper.getTmpFile("pcm", false)
        val ffmpegCommand = "-i $audio -f s16le -acodec pcm_s16le -ac 1 -ar 24000 $tmp"
        val session = FFmpegKit.execute(ffmpegCommand)
        if (!ReturnCode.isSuccess(session.returnCode)) {
            error("mp3 to pcm error: ${session.allLogsAsString}")
        }
        return tmp
    }

    private external fun pcmToSilk(pcmFile: String, silkFile: String): Double
}