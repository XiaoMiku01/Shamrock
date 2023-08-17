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
import mqq.app.MobileQQ

internal object DynamicReceiver: BroadcastReceiver() {
    private val hashHandler = mutableSetOf<IPCRequest>()
    private val cmdHandler = mutableMapOf<String, IPCRequest>()
    private val mutex = Mutex() // 滥用的锁，所以说尽量减少使用

    override fun onReceive(ctx: Context, intent: Intent) {
        val hash = intent.getIntExtra("hash", -1)
        val cmd = intent.getStringExtra("cmd") ?: ""
        kotlin.runCatching {
            if (cmd.isNotBlank()) {
                cmdHandler[cmd].also {
                    if (it == null) DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                        put("string", "无广播处理器: $cmd")
                    })
                }?.callback?.handle(intent)
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
        }.onFailure {
            DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                put("string", "处理器[$cmd]错误: $it")
            })
        }
    }

    fun register(cmd: String, request: IPCRequest) {
        cmdHandler[cmd] = request
    }

    fun unregister(cmd: String) {
        cmdHandler.remove(cmd)
    }

    fun register(request: IPCRequest) {
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