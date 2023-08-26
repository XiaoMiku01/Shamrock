package moe.fuqiuluo.remote

import com.tencent.mobileqq.helper.ShamrockConfig
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.fuqiuluo.remote.api.*
import moe.fuqiuluo.remote.config.contentNegotiation
import moe.fuqiuluo.remote.config.statusPages
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.internal.DataRequester

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
                contentNegotiation()
                statusPages()
                routing {
                    echoVersion()
                    obtainFrameworkInfo()
                    registerBDH()
                    userAction()
                    messageAction()
                    troopAction()
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
        DataRequester.request("success", mapOf("port" to currServerPort))
    }

    suspend fun changePort(port: Int) {
        if (this.currServerPort == port && isQueryServiceStarted) return
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