package moe.fuqiuluo.shamrock.ui.app

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import java.util.Date

object AppRuntime {
    var isInit: Boolean = false

    @SuppressLint("SimpleDateFormat")
    private val format = SimpleDateFormat("[HH:mm:ss] ")

    lateinit var state: RuntimeState

    lateinit var logger: Logger
    internal var maxLogSize = Short.MAX_VALUE / 2

    lateinit var requestCount: MutableIntState

    object AccountInfo {
        lateinit var uin: MutableState<String>

        lateinit var nick: MutableState<String>
    }

    fun log(msg: String, level: Level = Level.INFO) {
        if (::logger.isInitialized) {
            val format = "%s%s %s".format(format.format(Date()), level.name, msg)

            val buffer = logger.logCache

            if (logger.size.intValue >= maxLogSize || buffer.length > 30000) {
                buffer.setLength(0)
                logger.logRanges = mutableListOf()
                logger.size.intValue = 0
            }

            val start = buffer.length
            val end = start + format.length

            logger.size.intValue += format.length
            buffer.append(format)
            logger.logRanges.add(Logger.LogRange(start, end, level))
        } else {
            Log.e("AppRuntime", "logger is not initialized")
        }
    }
}

class RuntimeState(
    val isFined: MutableState<Boolean>,
    val coreVersion: MutableState<String>,
    val coreCode: MutableIntState,
    val coreName: MutableState<String>
) {

}

enum class Level(
    val color: Color,
    val id: Byte
) {
    DEBUG(Color(0xFF4CAF50), 0),
    INFO(Color(0xff6c6c6c), 1),
    WARN(Color(0xFFFF9800), 2),
    ERROR(Color(0xFFE91E63), 3),
}

class Logger(
    var logCache: StringBuffer,
    val size: MutableIntState,
    var logRanges: MutableList<LogRange>,
) {
    data class LogRange(
        val start: Int,
        val end: Int,
        val level: Level
    )
}

