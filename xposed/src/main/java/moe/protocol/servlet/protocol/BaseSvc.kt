package moe.protocol.servlet.protocol

import android.os.Bundle
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.qphone.base.remote.ToServiceMsg
import moe.protocol.servlet.utils.PlatformUtils
import mqq.app.MobileQQ
import tencent.im.oidb.oidb_sso

abstract class BaseSvc {
    protected val currentUin: String
        get() = app.currentAccountUin

    protected val app: QQAppInterface
        get() = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface

    protected fun createToServiceMsg(cmd: String): ToServiceMsg {
        return ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
    }

    protected fun send(toServiceMsg: ToServiceMsg) {
        app.sendToService(toServiceMsg)
    }

    protected fun sendExtra(cmd: String, builder: (Bundle) -> Unit) {
        val toServiceMsg = createToServiceMsg(cmd)
        builder(toServiceMsg.extraData)
        app.sendToService(toServiceMsg)
    }

    protected fun sendPb(cmd: String, buffer: ByteArray) {
        val toServiceMsg = createToServiceMsg(cmd)
        toServiceMsg.putWupBuffer(buffer)
        toServiceMsg.addAttribute("req_pb_protocol_flag", true)
        app.sendToService(toServiceMsg)
    }

    protected fun sendOidb(cmd: String, cmdId: Int, serviceId: Int, buffer: ByteArray) {
        val to = createToServiceMsg(cmd)
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