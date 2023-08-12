package moe.fuqiuluo.http.api

import com.tencent.mobileqq.fe.FEKit
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import moe.fuqiuluo.xposed.tools.fetchGet
import moe.fuqiuluo.xposed.tools.fetchPost
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import moe.fuqiuluo.xposed.tools.toHexString

fun Routing.sign() {
    get("/sign") {
        val uin = fetchGet("uin")
        val cmd = fetchGet("cmd")
        val seq = fetchGet("seq").toInt()
        val buffer = fetchGet("buffer").hex2ByteArray()

        requestSign(cmd, uin, seq, buffer)
    }

    post("/sign") {
        val param = call.receiveParameters()

        val uin = param.fetchPost("uin")
        val cmd = param.fetchPost("cmd")
        val seq = param.fetchPost("seq").toInt()
        val buffer = param.fetchPost("buffer").hex2ByteArray()

        requestSign(cmd, uin, seq, buffer)
    }
}

@Serializable
private data class Sign(
    val token: String,
    val extra: String,
    val sign: String,
    val o3did: String,
    val requestCallback: List<Int>
)

private suspend fun PipelineContext<Unit, ApplicationCall>.requestSign(
    cmd: String,
    uin: String,
    seq: Int,
    buffer: ByteArray,
) {
    val sign = FEKit.getInstance().getSign(cmd, buffer, seq, uin)
    call.respond(
        OldApiResult(0, "success",
            Sign(
                sign.token.toHexString(),
                sign.extra.toHexString(),
                sign.sign.toHexString(),
                "",
                listOf()
            )
        )
    )
}