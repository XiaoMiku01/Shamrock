package moe.fuqiuluo.xposed.helper

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.atomicfu.atomic
import moe.fuqiuluo.xposed.actions.impl.GlobalUi
import kotlin.concurrent.timer

object DataRequester {
    private val URI = Uri.parse("content://moe.fuqiuluo.xqbot.provider")
    private val seqFactory = atomic(0)
    private val seq: Int
        get() {
            if (seqFactory.value > 1000000) {
                seqFactory.lazySet(0)
            }
            return seqFactory.incrementAndGet()
        }

    fun request(
        ctx: Context,
        cmd: String,
        bodyBuilder: ContentValues.() -> Unit,
        onFailure: (Throwable) -> Unit,
        callback: ICallback
    ): Int {
        val values = ContentValues()
        values.bodyBuilder()
        val currentSeq = seq
        values.put("hash", (cmd + currentSeq).hashCode())
        values.put("cmd", cmd)

        kotlin.runCatching {
            ctx.contentResolver
                .insert(URI, values)
        }.onFailure {
            onFailure.invoke(it)
        }

        val job = timer(initialDelay = 6000L, period = 5000L) {
            DynamicReceiver.unregister(currentSeq)
            this.cancel()
        }
        val request = Request(cmd, currentSeq, values) {
            kotlin.runCatching {
                job.cancel()
            }
            callback.handle(it)
        }
        DynamicReceiver.register(request)

        return currentSeq
    }
}

fun interface ICallback {
    fun handle(intent: Intent)
}

data class Request(
    val cmd: String,
    val seq: Int,
    val values: ContentValues,
    val callback: ICallback
) {
    override fun hashCode(): Int {
        return (cmd + seq).hashCode()
    }
}