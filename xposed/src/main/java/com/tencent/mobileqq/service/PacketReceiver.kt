package com.tencent.mobileqq.service

import com.tencent.qphone.base.remote.FromServiceMsg
import moe.fuqiuluo.xposed.actions.impl.toast
import mqq.app.MobileQQ

internal object PacketReceiver {
    private val allowCommandList: MutableSet<String> by lazy { mutableSetOf(
        "SQQzoneSvc.getCover",
        "SummaryCard.ReqSummaryCard", // 名片
        "IncreaseURLSvr.QQHeadUrlReq", //  头像
        "SummaryCard.ReqSearch"
    ) }

    private fun msgFilter(cmd: String): Boolean {
        return cmd !in allowCommandList
    }

    fun onReceive(from: FromServiceMsg?) {
        if (from == null || msgFilter(from.serviceCmd)) return
        when (from.serviceCmd) {

        }
    }
}