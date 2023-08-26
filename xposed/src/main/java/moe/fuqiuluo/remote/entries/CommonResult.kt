package moe.fuqiuluo.remote.entries

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class Status(
    val code: Int
) {
    Ok(0),
    BadRequest(10001),
    UnsupportedAction(10002),
    BadParam(10003),
    UnsupportedParam(10004),
    UnsupportedSegment(10005),
    UnsupportedSegmentData(10007),
    BadSegmentData(10006),
    WhoAmI(10101),
    UnknownSelf(10102),
    BadHandler(20001),
    InternalHandlerError(20002),
    DatabaseError(31000),
    FilesystemError(32000),
    NetworkError(33000),
    PlatformError(34000),
    LogicError(35000),
    IAmTired(36000),
}

@Serializable
data class CommonResult<T>(
    var status: String,
    var retcode: Int,
    @Contextual
    var data: T,
    var message: String = "",
    var echo: String = ""
)

@Serializable
object EmptyObject

internal inline fun <reified T: Any> resultToString(
    isOk: Boolean,
    code: Status,
    data: T,
    msg: String = "",
    echo: String = ""
): String {
    return Json.encodeToString(
        CommonResult(if (isOk) "ok" else "failed", code.code, data, msg, echo)
    )
}

internal inline fun <reified T> result(
    isOk: Boolean,
    code: Status,
    data: T,
    msg: String = "",
    echo: String = ""
): CommonResult<T?> {
    return result(isOk, code.code, data, msg, echo)
}

internal inline fun <reified T> result(
    isOk: Boolean,
    code: Int,
    data: T,
    msg: String = "",
    echo: String = ""
): CommonResult<T?> {
    return CommonResult(
        if (isOk) "ok" else "failed",
        code,
        data,
        msg,
        echo
    )
}