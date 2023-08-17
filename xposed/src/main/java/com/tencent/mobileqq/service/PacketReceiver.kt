package com.tencent.mobileqq.service

import com.tencent.mobileqq.sign.QQSecuritySign
import com.tencent.qphone.base.remote.FromServiceMsg
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.http.action.helper.ContactHelper
import moe.fuqiuluo.http.action.helper.FileHelper
import moe.fuqiuluo.xposed.actions.impl.toast
import moe.fuqiuluo.xposed.helper.DataRequester
import moe.fuqiuluo.xposed.helper.DynamicReceiver
import moe.fuqiuluo.xposed.helper.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast
import mqq.app.MobileQQ
import tencent.im.oidb.cmd0x11b2.oidb_0x11b2
import tencent.im.oidb.oidb_sso
import kotlin.coroutines.resume

internal typealias PacketHandler = (FromServiceMsg) -> Unit

internal object PacketReceiver {
    private val allowCommandList: MutableSet<String> by lazy { mutableSetOf(
        "SQQzoneSvc.getCover",
        "SummaryCard.ReqSummaryCard", // 名片
        "IncreaseURLSvr.QQHeadUrlReq", //  头像
        "SummaryCard.ReqSearch",
    ) }
    private val HandlerByIpcSet = hashSetOf<String>()

    init {
        DynamicReceiver.register("register_handler_cmd", IPCRequest {
            val cmd = it.getStringExtra("handler_cmd")!!
            DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                put("string", "RegisterHandler(cmd = $cmd)")
            })
            HandlerByIpcSet.add(cmd)
        })
    }

    private fun msgFilter(cmd: String): Boolean {
        return cmd !in allowCommandList && cmd !in HandlerByIpcSet
    }

    private fun onReceive(from: FromServiceMsg) {
        if (HandlerByIpcSet.contains(from.serviceCmd)) {
            DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                put("string", "ReceivePacket(cmd = ${from.serviceCmd})")
            })
            MobileQQ.getContext().broadcast("xqbot") {
                putExtra("cmd", from.serviceCmd)
                putExtra("buffer", from.wupBuffer)
            }
        }
    }

    fun internalOnReceive(from: FromServiceMsg?) {
        if (from == null || msgFilter(from.serviceCmd)) return
        GlobalScope.launch {
            onReceive(from)
        }
    }
}