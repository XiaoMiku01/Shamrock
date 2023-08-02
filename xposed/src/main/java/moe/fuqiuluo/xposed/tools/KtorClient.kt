package moe.fuqiuluo.xposed.tools

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout

val GlobalClient: HttpClient by lazy {
    HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 3000
        }
    }
}