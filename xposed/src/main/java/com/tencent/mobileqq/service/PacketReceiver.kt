package com.tencent.mobileqq.service

import com.tencent.qphone.base.remote.FromServiceMsg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.internal.DataRequester
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.broadcast
import mqq.app.MobileQQ

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
            LogCenter.log("RegisterHandler(cmd = $cmd)", Level.DEBUG)
            HandlerByIpcSet.add(cmd)
        })
        DynamicReceiver.register("unregister_handler_cmd", IPCRequest {
            val cmd = it.getStringExtra("handler_cmd")!!
            LogCenter.log("UnRegisterHandler(cmd = $cmd)", Level.DEBUG)
            HandlerByIpcSet.remove(cmd)
        })
    }

    private fun msgFilter(cmd: String): Boolean {
        return cmd !in allowCommandList && cmd !in HandlerByIpcSet
    }

    private fun onReceive(from: FromServiceMsg) {
        if (HandlerByIpcSet.contains(from.serviceCmd)) {
            DataRequester.request("send_message", bodyBuilder = {
                put("string", "ReceivePacket(cmd = ${from.serviceCmd})")
            })
            MobileQQ.getContext().broadcast("xqbot") {
                putExtra("__cmd", from.serviceCmd)
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