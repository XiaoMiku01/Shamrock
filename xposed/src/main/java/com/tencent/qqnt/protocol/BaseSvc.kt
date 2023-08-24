package com.tencent.qqnt.protocol

import android.os.Bundle
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qqnt.utils.PlatformUtils
import mqq.app.MobileQQ
import tencent.im.oidb.oidb_sso

abstract class BaseSvc {
    protected val currentUin: String
        get() = (MobileQQ.getMobileQQ() as QQAppInterface).currentAccountUin

    protected fun sendExtra(cmd: String, builder: (Bundle) -> Unit) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val toServiceMsg = ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
        builder(toServiceMsg.extraData)
        app.sendToService(toServiceMsg)
    }

    protected fun sendPb(cmd: String, buffer: ByteArray) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val toServiceMsg = ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
        toServiceMsg.putWupBuffer(buffer)
        toServiceMsg.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(toServiceMsg)
    }

    protected fun sendOidb(cmd: String, cmdId: Int, serviceId: Int, buffer: ByteArray) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface
        val to = ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
        val oidb = oidb_sso.OIDBSSOPkg()
        oidb.uint32_command.set(cmdId)
        oidb.uint32_service_type.set(serviceId)
        oidb.bytes_bodybuffer.set(ByteStringMicro.copyFrom(buffer))
        oidb.str_client_version.set(PlatformUtils.getClientVersion(MobileQQ.getContext()))
        to.putWupBuffer(oidb.toByteArray())
        to.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(to)
    }
}