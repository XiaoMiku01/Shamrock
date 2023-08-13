@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import com.tencent.mobileqq.service.PacketReceiver
import com.tencent.msf.service.protocol.pb.SSOLoginMerge
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.util.CodecWarpper
import kotlinx.atomicfu.atomic
import moe.fuqiuluo.xposed.actions.IAction
import de.robv.android.xposed.XposedBridge.log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.tools.hookMethod
import moe.fuqiuluo.xposed.tools.slice

class RegisterServiceHandler: IAction {
    override fun invoke(ctx: Context) {
        kotlin.runCatching {
            val isInit = atomic(false)
            CodecWarpper::class.java.hookMethod("init").after {
                if (isInit.value) return@after
                hookReceive(it.thisObject.javaClass)
                isInit.lazySet(true)
            }
            CodecWarpper::class.java.hookMethod("nativeOnReceData").before {
                if (isInit.value) return@before
                hookReceive(it.thisObject.javaClass)
                isInit.lazySet(true)
            }
        }.onFailure {
            log(it)
        }
    }

    private fun hookReceive(thizClass: Class<*>) {
        thizClass.hookMethod("onResponse").before {
            val from = it.args[1] as FromServiceMsg
            GlobalScope.launch {
                kotlin.runCatching {
                    if ("SSO.LoginMerge" == from.serviceCmd) {
                        val packetList = SSOLoginMerge.BusiBuffData()
                            .mergeFrom(from.wupBuffer.slice(4))
                            .BusiBuffVec.get()
                        packetList.forEach {
                            PacketReceiver.onReceive(FromServiceMsg().apply {
                                this.requestSsoSeq = it.SeqNo.get()
                                this.serviceCmd = it.ServiceCmd.get()
                                putWupBuffer(it.BusiBuff.get().toByteArray())
                            })
                        }
                    } else PacketReceiver.onReceive(from)
                }
            }
        }
    }
}



