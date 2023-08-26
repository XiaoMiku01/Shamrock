@file:OptIn(DelicateCoroutinesApi::class, ObsoleteCoroutinesApi::class)
package moe.fuqiuluo.utils

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

object DownloadUtils {
    private const val MAX_THREAD = 4

    suspend fun download(urlAdr: String, dest: File) {
        val url = URL(urlAdr)
        val connection = withContext(Dispatchers.IO) { url.openConnection() } as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        val responseCode = connection.responseCode
        if (responseCode == 200) {
            val contentLength = connection.contentLength
            withContext(Dispatchers.IO) {
                val raf = RandomAccessFile(dest, "rw")
                raf.setLength(contentLength.toLong())
                raf.close()
            }
            var threadCnt = MAX_THREAD
            if (contentLength <= 1024 * 1024) {
                threadCnt = 1
            }
            var blockSize = (contentLength * (1.0 / threadCnt)).roundToInt()
            connection.disconnect()
            val progress = atomic(0)
            val channel = Channel<Int>()
            var processed = 0
            for (i in 0 until threadCnt) {
                if (processed + blockSize != contentLength && i == threadCnt - 1) {
                    blockSize = contentLength - processed
                }
                val start = i * blockSize
                var end = (i + 1) * blockSize - 1
                if (i == 3 - 1) {
                    end = contentLength - 1
                }
                GlobalScope.launch(Dispatchers.IO) {
                    reallyDownload(url, start, end, dest, channel)
                }
                processed += blockSize
            }
            withTimeoutOrNull(60000L) {
                while (progress.value < contentLength) {
                    if(progress.addAndGet(channel.receive()) >= contentLength) {
                        break
                    }
                }
            } ?: dest.delete()
        }
    }

    private suspend fun reallyDownload(url: URL, start: Int, end: Int, dest: File, channel: Channel<Int>) {
        val openConnection: HttpURLConnection = withContext(Dispatchers.IO) { url.openConnection() } as HttpURLConnection
        openConnection.requestMethod = "GET"
        openConnection.connectTimeout = 5000
        openConnection.setRequestProperty("range", "bytes=$start-$end")
        val responseCode = openConnection.responseCode
        if (responseCode == 206) {
            val inputStream = openConnection.inputStream
            val raf = withContext(Dispatchers.IO) {
                RandomAccessFile(dest, "rw").also {
                    it.seek(start.toLong())
                }
            }
            var len: Int
            val buf = ByteArray(1024)
            var flag = true
            while (flag) {
                len = withContext(Dispatchers.IO) {
                    inputStream.read(buf)
                }
                flag = len != -1
                if (flag) {
                    withContext(Dispatchers.IO) {
                        raf.write(buf, 0, len)
                    }
                }
                channel.send(len)
            }
            withContext(Dispatchers.IO) {
                inputStream.close()
                raf.close()
            }
        }
        openConnection.disconnect()
    }

}