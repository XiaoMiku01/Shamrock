package moe.protocol.service.api

import moe.protocol.service.config.ShamrockConfig
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.GlobalClient
import java.net.SocketException

internal abstract class HttpPushServlet: BasePushServlet {
     override val address: String
         get() = "http://" + ShamrockConfig.getWebHookAddress()

     override fun allowPush(): Boolean {
         return ShamrockConfig.allowWebHook()
     }

    protected suspend inline fun <reified T> pushTo(body: T): HttpResponse? {
        if(!allowPush()) return null
        try {
            return GlobalClient.post(address) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (e: ConnectTimeoutException) {
            LogCenter.log("HTTP推送失败: 请检查你的推送服务器。", Level.ERROR)
        } catch (e: SocketException) {
            LogCenter.log("HTTP推送失败: 网络波动。", Level.ERROR)
        } catch (e: HttpRequestTimeoutException) {
            LogCenter.log("HTTP推送失败: 推送服务器无法连接。", Level.ERROR)
        } catch (e: Throwable) {
            LogCenter.log("HTTP推送失败: ${e.stackTraceToString()}", Level.ERROR)
        }
        return null
    }
}