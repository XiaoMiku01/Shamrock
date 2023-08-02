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

object DynamicReceiver: BroadcastReceiver() {
    private val mutableSet = mutableSetOf<Request>()
    private val mutex = Mutex() // 滥用的锁，所以说尽量减少使用

    override fun onReceive(ctx: Context, intent: Intent) {
        GlobalScope.launch { mutex.withLock {
            mutableSet.forEach {
                val hash = intent.getIntExtra("hash", -1)
                if (hash == -1) return@forEach

                if (hash == it.hashCode()) {
                    it.callback.handle(intent)
                    mutableSet.remove(it)
                    return@forEach
                }
            }
        } }
    }

    fun register(request: Request) {
        GlobalScope.launch {
            mutex.withLock {
                mutableSet.add(request)
            }
        }
    }

    fun unregister(seq: Int) {
        GlobalScope.launch {
            mutex.withLock {
                mutableSet.removeIf {
                    it.seq == seq
                }
            }
        }
    }
}