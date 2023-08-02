package moe.fuqiuluo.http

import android.widget.Toast
import androidx.core.content.edit
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
import moe.fuqiuluo.http.api.getAccountInfo
import moe.fuqiuluo.http.api.getMsfInfo
import moe.fuqiuluo.http.api.getStartTime
import moe.fuqiuluo.http.api.uploadGroupImage
import moe.fuqiuluo.xposed.actions.impl.GlobalUi
import moe.fuqiuluo.xposed.actions.impl.PullConfig
import moe.fuqiuluo.xposed.helper.DataRequester
import moe.fuqiuluo.xposed.loader.ActionLoader
import mqq.app.MobileQQ
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object HTTPServer {
    var isQueryServiceStarted = false
    internal var startTime = 0L

    private val API_LIST = arrayOf(
        Routing::index to false,
        Routing::getAccountInfo to false,
        Routing::getMsfInfo to true,
        Routing::getStartTime to false,
        Routing::uploadGroupImage to true
    )
    private val mutex = Mutex()
    private lateinit var server: ApplicationEngine
    internal var PORT: Int = 0

    suspend fun start(port: Int) {
        if (isQueryServiceStarted) return
        mutex.withLock {
            val ctx = MobileQQ.getContext()
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
                        call.respond(CommonResult("ok", -1, ErrorCatch(
                            call.request.uri, cause.stackTraceToString())
                        ))
                    }
                }
                routing {
                    kotlin.runCatching {
                        val shamrockConfig = ctx.getSharedPreferences("shamrock_config", 0)
                        val proApi = shamrockConfig.getBoolean("pro_api", false)
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
            log("Start HTTP Server: http://0.0.0.0:$PORT/")

            DataRequester.request(ctx, "success", bodyBuilder = {
                put("port", PORT)
            }, onFailure = {}) {}
        }
    }

    suspend fun change(port: Int) {
        if (this.PORT == port && isQueryServiceStarted) return
        stop()
        start(port)
    }

    suspend fun stop() {
        mutex.withLock {
            server.stop()
            isQueryServiceStarted = false
        }
    }
}