package moe.fuqiuluo.http.action.helper.codec

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.PipedInputStream
import java.io.PipedOutputStream

internal abstract class BaseCodecProcessor(
    protected val frameSize: Int
): Closeable {
    companion object {
        val SampleRateMap = intArrayOf(8000, 12000, 16000, 24000, 36000, 44100, 48000)

        fun calcFrameSize(sampleRate: Int): Int {
            return sampleRate * 20 * 2 / 1000
        }
    }

    private val output = PipedOutputStream()
    private val input = PipedInputStream(output, 1920)

    protected val inBuf = ByteArray(frameSize)
    protected val outBuf = ByteArray(frameSize)
    protected var processedData = ByteArray(1024 * 1024 * 10)
    protected var dataOffset: Int = 0

    abstract fun encodeCodec()

    protected fun process(data: ByteArray, offset: Int, len: Int) {
        output.write(data, offset, len)
        while (true) {
            if (input.available() < this.frameSize) {
                return
            }
            if (input.read(inBuf, 0, inBuf.size) == -1) {
                return
            }
            encodeCodec()
        }
    }

    fun doProcess(data: ByteArray) {
        var size = data.size
        var offset = 0
        while (size > 0) {
            var realSize = 1920 - input.available()
            if (size <= realSize) {
                realSize = size
            }
            process(data, offset, realSize)
            offset += realSize
            size -= realSize
        }
    }

    fun getData(): ByteArray {
        return processedData
            .slice(0 .. dataOffset)
            .toByteArray()
    }

    override fun close() {
        input.close()
        output.close()
    }
}