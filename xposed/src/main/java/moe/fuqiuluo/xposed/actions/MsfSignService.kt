package moe.fuqiuluo.xposed.actions

import android.content.Context
import com.tencent.mobileqq.fe.FEKit
import moe.fuqiuluo.xposed.actions.IAction
import com.tencent.qqnt.utils.PlatformUtils
import moe.fuqiuluo.xposed.helper.internal.DynamicReceiver
import moe.fuqiuluo.xposed.helper.internal.IPCRequest
import moe.fuqiuluo.xposed.tools.broadcast

internal class MsfSignService: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMsfProcess()) return

        DynamicReceiver.register("sign", IPCRequest {
            val cmd = it.getStringExtra("wupCmd")
            val seq = it.getIntExtra("seq", -1)
            val buffer = it.getByteArrayExtra("buffer")
            val uin = it.getStringExtra("uin")
            val sign = FEKit.getInstance().getSign(cmd, buffer, seq, uin)
            ctx.broadcast("xqbot") {
                putExtra("__cmd", "sign_callback")
                putExtra("sign", sign.sign)
                putExtra("token", sign.token)
                putExtra("extra", sign.extra)
            }
        })
    }
}