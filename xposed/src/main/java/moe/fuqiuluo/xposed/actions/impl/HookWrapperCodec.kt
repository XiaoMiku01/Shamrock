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
import moe.fuqiuluo.xposed.tools.hookMethod
import moe.fuqiuluo.xposed.tools.slice

class HookWrapperCodec: IAction {
    private val IgnoredCmd = arrayOf(
        "trpc.sq_adv.official_account_adv_push.OfficialAccountAdvPush.AdvPush",
        "LightAppSvc.mini_app_report_transfer.DataReport",
        "JsApiSvr.webview.whitelist",
        "trpc.commercial.access.access_sso.SsoAdGet",
        "trpc.qpay.homepage2.Homepage2.SsoGetHomepage",
        "trpc.qpay.value_added_info.Query.SsoGetPrivilege",
        "trpc.qqshop.qgghomepage.Config.SsoGetBottomTab",
        "ClubInfoSvc.queryPrivExt"
    )

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
            kotlin.runCatching {
                if ("SSO.LoginMerge" == from.serviceCmd) {
                    val merge = SSOLoginMerge.BusiBuffData()
                        .mergeFrom(from.wupBuffer.slice(4))
                    val busiBufVec = merge.BusiBuffVec.get()
                    busiBufVec.forEach { item ->
                        if (item.ServiceCmd.get() in IgnoredCmd) {
                            busiBufVec.remove(item)
                        } else {
                            pushOnReceive(FromServiceMsg().apply {
                                this.requestSsoSeq = item.SeqNo.get()
                                this.serviceCmd = item.ServiceCmd.get()
                                putWupBuffer(item.BusiBuff.get().toByteArray())
                            })
                        }
                    }
                    merge.BusiBuffVec.set(busiBufVec)
                    from.putWupBuffer(merge.toByteArray())
                    it.args[1] = from
                } else {
                    if (from.serviceCmd in IgnoredCmd) {
                        from.serviceCmd = "ShamrockInjectedCmd"
                        it.args[1] = from
                    } else {
                        pushOnReceive(from)
                    }
                }
            }

        }
    }

    private fun pushOnReceive(fromServiceMsg: FromServiceMsg) {
        PacketReceiver.internalOnReceive(fromServiceMsg)
    }
}



