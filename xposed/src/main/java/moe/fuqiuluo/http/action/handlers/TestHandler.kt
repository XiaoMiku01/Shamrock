package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import com.tencent.qqnt.msg.api.IMsgService
import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import de.robv.android.xposed.XposedBridge.log

internal object TestHandler: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        kotlin.runCatching {
            /*val service = QRoute.api(IMsgService::class.java)
            service.sendMsg(
                Contact(MsgConstant.KCHATTYPEGROUP, "645830205", ""),
                arrayListOf(MsgElement().apply {
                    this.elementType = MsgConstant.KELEMTYPETEXT

                    textElement = TextElement().apply {
                        content = "NTQQ消息测试"
                    }
                })
            ) { i, str ->
                log("sendMsg: $i, $str")
            }*/
            return resultToString(
                isOk = true,
                code = Status.Ok,
                data = Test(System.currentTimeMillis())
            )
        }.onFailure {
            log(it)
        }
        return "error"
    }
    override fun path(): String = "test"
    @Serializable
    data class Test(val time: Long)
}