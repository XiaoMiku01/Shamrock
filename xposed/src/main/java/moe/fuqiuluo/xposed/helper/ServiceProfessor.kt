@file:OptIn(DelicateCoroutinesApi::class)

package moe.fuqiuluo.xposed.helper

import android.content.ContentValues
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object ServiceProfessor {
    private val hashHandler = mutableSetOf<Request>()
    private val cmdHandler = mutableMapOf<String, Request>()
    private val mutex = Mutex() // 滥用的锁，所以说尽量减少使用

    fun onReceive(values: ContentValues) {
    }

    fun register(cmd: String, request: Request) {
        cmdHandler[cmd] = request
    }

    fun register(request: Request) {
        GlobalScope.launch {
            mutex.withLock {
                hashHandler.add(request)
            }
        }
    }

    fun unregister(seq: Int) {
        GlobalScope.launch {
            mutex.withLock {
                hashHandler.removeIf {
                    it.seq == seq
                }
            }
        }
    }
}