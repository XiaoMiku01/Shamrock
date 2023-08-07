package com.tencent.mobileqq.service

import android.os.SystemClock
import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.app.BusinessHandler
import com.tencent.mobileqq.profilecard.api.IProfileProtocolConst
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.remote.ToServiceMsg
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
        from?.let {
            MobileQQ.getContext().toast("Receive：${it.serviceCmd}")
        }
        if (from == null || msgFilter(from.serviceCmd)) return
        when (from.serviceCmd) {
            "SummaryCard.ReqSummaryCard" -> {
                MobileQQ.getContext().toast("资料卡获取成功")
            }
            "IncreaseURLSvr.QQHeadUrlReq" -> {
                MobileQQ.getContext().toast("头像获取成功")
            }
            "SummaryCard.ReqSearch" -> {
                MobileQQ.getContext().toast("搜索成功")
            }
        }
    }
}