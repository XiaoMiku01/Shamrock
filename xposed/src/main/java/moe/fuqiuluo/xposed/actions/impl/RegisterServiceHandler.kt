@file:OptIn(DelicateCoroutinesApi::class, ExperimentalSerializationApi::class)
package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.app.BusinessHandler
import com.tencent.mobileqq.service.PacketReceiver
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.util.CodecWarpper
import kotlinx.atomicfu.atomic
import moe.fuqiuluo.xposed.actions.IAction
import mqq.app.MobileQQ
import de.robv.android.xposed.XposedBridge.log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import moe.fuqiuluo.xposed.tools.hookMethod
import moe.fuqiuluo.xposed.tools.slice

class RegisterServiceHandler: IAction {
    override fun invoke(ctx: Context) {
        //if (!MobileQQ.getMobileQQ().qqProcessName.contains("msf", ignoreCase = true)) return
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
                if ("SSO.LoginMerge" == from.serviceCmd) {
                    ProtoBuf.decodeFromByteArray(
                        SSOLoginMerge.BusiBuffData.serializer(),
                        from.wupBuffer.slice(4)
                    ).buffList?.forEach {
                        PacketReceiver.onReceive(FromServiceMsg().apply {
                            requestSsoSeq = it.seq
                            putWupBuffer(it.data)
                            serviceCmd = it.cmd
                        })
                    }
                } else PacketReceiver.onReceive(from)
            }
        }
    }

    class SSOLoginMerge {
        @Serializable
        data class BusiBuffData(
            @ProtoNumber(number = 1) @JvmField var buffList: ArrayList<BusiBuffItem>? = null,
            @ProtoNumber(number = 2) @JvmField var MaxRespSizeHint: Int? = null,
        ) {
            fun add(seq: Int, cmd: String, data: ByteArray, needResp: Boolean = true) {
                if (buffList == null) {
                    buffList = arrayListOf()
                }
                buffList?.add(
                    BusiBuffItem(
                        seq, cmd, data.size, data, needResp
                    )
                )
            }
        }

        @Serializable
        data class BusiBuffItem(
            @ProtoNumber(number = 1) @JvmField var seq: Int = 0,
            @ProtoNumber(number = 2) @JvmField var cmd: String = "",
            @ProtoNumber(number = 3) @JvmField var size: Int = 0,
            @ProtoNumber(number = 4) @JvmField var data: ByteArray? = null,
            @ProtoNumber(number = 5) @JvmField var needResp: Boolean = false
        )
    }
}