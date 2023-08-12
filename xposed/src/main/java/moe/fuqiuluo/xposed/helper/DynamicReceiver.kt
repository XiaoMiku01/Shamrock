@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object DynamicReceiver: BroadcastReceiver() {
    private val hashHandler = mutableSetOf<Request>()
    private val cmdHandler = mutableMapOf<String, Request>()
    private val mutex = Mutex() // 滥用的锁，所以说尽量减少使用

    override fun onReceive(ctx: Context, intent: Intent) {
        val hash = intent.getIntExtra("hash", -1)
        val cmd = intent.getStringExtra("cmd") ?: ""
        if (cmd.isNotBlank()) {
            cmdHandler[cmd]?.callback?.handle(intent)
        } else GlobalScope.launch { mutex.withLock {
            hashHandler.forEach {
                if (hash == -1) return@forEach

                if (hash == it.hashCode()) {
                    it.callback?.handle(intent)
                    if (it.seq != -1)
                        hashHandler.remove(it)
                    return@forEach
                }
            }
        } }
    }

    fun register(cmd: String, request: Request) {
        cmdHandler[cmd] = request
    }

    fun unregister(cmd: String) {
        cmdHandler.remove(cmd)
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