package moe.protocol.service.api

import com.tencent.mobileqq.app.QQAppInterface
import mqq.app.MobileQQ

internal interface BasePushServlet {
    val address: String

    fun allowPush(): Boolean

    val app: QQAppInterface
        get() = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
}