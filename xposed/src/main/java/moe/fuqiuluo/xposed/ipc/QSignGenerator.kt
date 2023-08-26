package moe.fuqiuluo.xposed.ipc

import com.tencent.mobileqq.fe.FEKit

internal object QSignGenerator: IQSigner.Stub() {
    override fun sign(cmd: String, seq: Int, uin: String, buffer: ByteArray): IQSign {
        val sign = FEKit.getInstance().getSign(cmd, buffer, seq, uin)
        return IQSign(sign.token, sign.extra, sign.sign,)
    }
}