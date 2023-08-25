package moe.fuqiuluo.http

import com.tencent.mobileqq.helper.ShamrockConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import moe.fuqiuluo.http.api.index
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.ErrorCatch
import de.robv.android.xposed.XposedBridge.log
import com.tencent.qqnt.msg.LogicException
import com.tencent.qqnt.msg.ParamsException
import moe.fuqiuluo.http.api.banTroopMember
import moe.fuqiuluo.http.api.energy
import moe.fuqiuluo.http.api.getAccountInfo
import moe.fuqiuluo.http.api.getMsfInfo
import moe.fuqiuluo.http.api.getMsg
import moe.fuqiuluo.http.api.getStartTime
import moe.fuqiuluo.http.api.isBlackListUin
import moe.fuqiuluo.http.api.kickTroopMember
import moe.fuqiuluo.http.api.sendGroupMessage
import moe.fuqiuluo.http.api.sendLike
import moe.fuqiuluo.http.api.setProfileCard
import moe.fuqiuluo.http.api.shut
import moe.fuqiuluo.http.api.sign
import moe.fuqiuluo.http.api.uploadGroupImage
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import mqq.app.MobileQQ

// 接口名称-------是否需要打开专业级开关
private val API_LIST = arrayOf(
    Routing::index to false,
    Routing::getAccountInfo to false,
    Routing::getMsfInfo to true,
    Routing::getStartTime to false,
    Routing::uploadGroupImage to true,
    Routing::energy to true,
    Routing::sign to true,
    Routing::isBlackListUin to false,
    Routing::setProfileCard to false,
    Routing::shut to false,
    Routing::sendGroupMessage to false,
    Routing::getMsg to false,
    Routing::sendLike to false,
    Routing::kickTroopMember to false,
    Routing::banTroopMember to false
)

object HTTPServer {
    @JvmStatic
    var isQueryServiceStarted = false
    internal var startTime = 0L

    private val actionMutex = Mutex()
    private lateinit var server: ApplicationEngine
    internal var PORT: Int = 0

    suspend fun start(port: Int) {
        if (isQueryServiceStarted) return
        actionMutex.withLock {
            server = embeddedServer(Netty, port = port) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(StatusPages) {
                    exception<Throwable> { call, cause ->
                        call.respond(when (cause) {
                            is ParamsException -> CommonResult("failed", Status.BadParam.code, ErrorCatch(call.request.uri, cause.message ?: ""))
                            is LogicException -> CommonResult("failed", Status.LogicError.code, ErrorCatch(call.request.uri, cause.message ?: ""))
                            else -> CommonResult("failed", Status.InternalHandlerError.code, ErrorCatch(call.request.uri, cause.stackTraceToString()))
                        })
                    }
                }
                routing {
                    kotlin.runCatching {
                        val proApi = ShamrockConfig.isPro()
                        API_LIST.forEach {
                            if (!it.second || proApi) {
                                it.first.invoke(this)
                            }
                        }
                    }.onFailure { log(it) }
                }
            }
            server.start(wait = false)
            startTime = System.currentTimeMillis()
            isQueryServiceStarted = true
            this.PORT = port

            LogCenter.log("Start HTTP Server: http://0.0.0.0:$PORT/")

            DataRequester.request("success", mapOf("port" to PORT))
        }
    }

    suspend fun changePort(port: Int) {
        if (this.PORT == port && isQueryServiceStarted) return
        stop()
        start(port)
    }

    suspend fun stop() {
        actionMutex.withLock {
            server.stop()
            isQueryServiceStarted = false
        }
    }
}