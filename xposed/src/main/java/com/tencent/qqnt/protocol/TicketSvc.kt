package com.tencent.qqnt.protocol

import com.tencent.mobileqq.app.QQAppInterface
import mqq.manager.TicketManager

internal object TicketSvc: BaseSvc() {
    object SigType {
        const val WLOGIN_A2 = 64
        const val WLOGIN_A5 = 2
        const val WLOGIN_AQSIG = 2097152
        const val WLOGIN_D2 = 262144
        const val WLOGIN_DA2 = 33554432
        const val WLOGIN_LHSIG = 4194304
        const val WLOGIN_LSKEY = 512
        const val WLOGIN_OPENKEY = 16384
        const val WLOGIN_PAYTOKEN = 8388608
        const val WLOGIN_PF = 16777216
        const val WLOGIN_PSKEY = 1048576
        const val WLOGIN_PT4Token = 134217728
        const val WLOGIN_QRPUSH = 67108864
        const val WLOGIN_RESERVED = 16
        const val WLOGIN_SID = 524288
        const val WLOGIN_SIG64 = 8192
        const val WLOGIN_SKEY = 4096
        const val WLOGIN_ST = 128
        const val WLOGIN_STWEB = 32 // TLV 103
        const val WLOGIN_TOKEN = 32768
        const val WLOGIN_VKEY = 131072
    }


    fun getStWeb(uin: String): String {
        return (app.getManager(QQAppInterface.TICKET_MANAGER) as TicketManager).getStweb(uin)
    }

}