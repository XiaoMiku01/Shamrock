package moe.protocol.service.listener

import com.tencent.qqnt.kernel.nativeinterface.IQQNTWrapperNetworkListener
import com.tencent.qqnt.kernel.nativeinterface.NetStatusType
import moe.fuqiuluo.xposed.helper.LogCenter

internal object NetworkListener: IQQNTWrapperNetworkListener {
    override fun onNetworkStatusChanged(o: NetStatusType, n: NetStatusType) {
        LogCenter.log("网络波动: $o -> $n")
    }
}