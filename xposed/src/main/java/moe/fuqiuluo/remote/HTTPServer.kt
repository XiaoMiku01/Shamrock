package moe.fuqiuluo.remote

import com.tencent.mobileqq.helper.ShamrockConfig
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.fuqiuluo.remote.api.*
import moe.fuqiuluo.remote.config.contentNegotiation
import moe.fuqiuluo.remote.config.statusPages
import moe.fuqiuluo.remote.plugin.Auth
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import moe.fuqiuluo.xposed.loader.NativeLoader

object HTTPServer {
    @JvmStatic
    var isQueryServiceStarted = false
    internal var startTime = 0L

    private val actionMutex = Mutex()
    private lateinit var server: ApplicationEngine
    internal var currServerPort: Int = 0

    suspend fun start(port: Int) {
        if (isQueryServiceStarted) return
        actionMutex.withLock {
            server = embeddedServer(Netty, port = port) {
                install(Auth)
                contentNegotiation()
                statusPages()
                routing {
                    echoVersion()
                    obtainFrameworkInfo()
                    registerBDH()
                    userAction()
                    messageAction()
                    troopAction()
                    friendAction()
                    ticketActions()
                    fetchRes()
                    if (ShamrockConfig.isPro()) {
                        qsign()
                        obtainProtocolData()
                    }
                }
            }
            server.start(wait = false)
        }
        startTime = System.currentTimeMillis()
        isQueryServiceStarted = true
        this.currServerPort = port
        LogCenter.log("Start HTTP Server: http://0.0.0.0:$currServerPort/")
        DataRequester.request("success", mapOf(
            "port" to currServerPort,
            "voice" to NativeLoader.isVoiceLoaded
        ))
    }

    fun isActive(): Boolean {
        return server.application.isActive
    }

    fun changePort(port: Int) {
        if (this.currServerPort == port && isQueryServiceStarted) return
        GlobalScope.launch {
            stop()
            start(port)
        }
    }

    suspend fun stop() {
        actionMutex.withLock {
            server.stop()
            isQueryServiceStarted = false
        }
    }
}