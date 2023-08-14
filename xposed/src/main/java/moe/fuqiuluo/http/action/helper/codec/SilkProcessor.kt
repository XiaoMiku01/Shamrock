package moe.fuqiuluo.http.action.helper.codec

import com.arthenica.ffmpegkit.FFmpegKit
import com.tencent.mobileqq.utils.SilkCodecWrapper
import mqq.app.MobileQQ
import java.io.File

internal class SilkProcessor(
    private val isEncoder: Boolean
): BaseCodecProcessor(calcFrameSize(SampleRateMap[2])) {
    private val codec: SilkCodecWrapper = SilkCodecWrapper(MobileQQ.getContext(), isEncoder)
    private var nativeRef: Long = if (isEncoder)
        codec.SilkEncoderNew(SampleRateMap[2], 0)
    else
        codec.SilkDecoderNew(SampleRateMap[2], 0)

    override fun encodeCodec() {
        if (isEncoder) {
            val size = codec.encode(nativeRef, inBuf, outBuf, inBuf.size)
            if (size > 0) {
                processedData[dataOffset] = (size and 255).toByte()
                processedData[dataOffset + 1] = (size shr 8 and 255).toByte()
                System.arraycopy(outBuf, 0, processedData, dataOffset + 2, size)
                dataOffset += 2 + size
            } else {
                error("pcm to silk error: $size")
            }
        }
    }

    fun doProcess(file: File) {
        doProcess(file.readBytes())
    }

    fun outputToFile(file: File) {
        file.outputStream().use {
            it.write(2)
            it.write("#!SILK_V3".toByteArray())
            it.write(getData())
        }
    }

    override fun close() {
        super.close()
        if (nativeRef != 0L) {
            codec.deleteCodec(nativeRef)
            nativeRef = 0
        }
    }
}