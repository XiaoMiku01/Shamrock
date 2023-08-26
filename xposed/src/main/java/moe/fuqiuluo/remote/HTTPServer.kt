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

// 接口名称-------是否需要打开专业级开关
private val API_LIST = arrayOf(
    Routing::getAccountInfo to false,
    Routing::getMsfInfo to true,
    Routing::getStartTime to false,
    Routing::uploadGroupImage to true,
    Routing::energy to true,
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