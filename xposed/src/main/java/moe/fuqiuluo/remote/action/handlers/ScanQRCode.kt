package moe.fuqiuluo.remote.action.handlers

import android.util.Base64
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.qrscan.api.IQRCodeApi
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

// TODO
internal object ScanQRCode: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val qrcode = QRoute.api(IQRCodeApi::class.java)
        val picBytes = Base64.decode(session.getString("pic"), Base64.DEFAULT)
        qrcode.scanImage(picBytes, 0, picBytes.size)
        return qrcode.version
    }

    override fun path(): String {
        return "sanc_qrcode"
    }
}