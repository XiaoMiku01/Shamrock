@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
package moe.fuqiuluo.http.api

import com.tencent.mobileqq.sign.QQSecuritySign.SignResult
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import moe.fuqiuluo.xposed.tools.fetchGet
import moe.fuqiuluo.xposed.tools.fetchPost
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import moe.fuqiuluo.xposed.tools.toHexString
import mqq.app.MobileQQ
import kotlin.coroutines.resume

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

private val reqLock = Mutex() // 懒得做高并发支持，写个锁，能用就行

private suspend fun PipelineContext<Unit, ApplicationCall>.requestSign(
    cmd: String,
    uin: String,
    seq: Int,
    buffer: ByteArray,
) {
    val sign = reqLock.withLock {
        withTimeoutOrNull(5000) {
            suspendCancellableCoroutine { con ->
                DynamicReceiver.register("sign_callback", IPCRequest {
                    con.resume(SignResult().apply {
                        this.sign = it.getByteArrayExtra("sign") ?: error("无法获取SIGN")
                        this.token = it.getByteArrayExtra("token")
                        this.extra = it.getByteArrayExtra("extra")
                    })
                })
                MobileQQ.getContext().broadcast("msf") {
                    putExtra("__cmd", "sign")
                    putExtra("wupCmd", cmd)
                    putExtra("uin", uin)
                    putExtra("seq", seq)
                    putExtra("buffer", buffer)
                }
                con.invokeOnCancellation {
                    DynamicReceiver.unregister("sign")
                    con.resume(SignResult())
                }
            }
        }
    } ?: SignResult()

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