package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.app.BusinessObserver
import com.tencent.mobileqq.app.MessageHandler
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.data.MessageRecord
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.helper.DataRequester
import mqq.app.MobileQQ
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object MessageHelper {
    private lateinit var METHOD_SEND_MSG: Method

    fun hookSendMessageOldChannel() {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is QQAppInterface)
            return

        val handler = app.msgHandler
        // final MessageRecord messageRecord, final BusinessObserver businessObserver, final boolean z
        if (!::METHOD_SEND_MSG.isInitialized) {
            METHOD_SEND_MSG = MessageHandler::class.java.declaredMethods.first {
                //XposedBridge.log("method: ${it.name}, params: ${it.parameterTypes.joinToString(", ")}")
                !Modifier.isPrivate(it.modifiers)
                && it.parameterCount == 3
                && it.parameterTypes[0] == MessageRecord::class.java
                && it.parameterTypes[1] == BusinessObserver::class.java
            }
        }

        if (::METHOD_SEND_MSG.isInitialized) {
            XposedBridge.hookMethod(METHOD_SEND_MSG, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val record = param.args[0] as MessageRecord
                    DataRequester.request(MobileQQ.getContext(), "send_message", bodyBuilder = {
                        put("string", record.toString())
                    })
                }
            })
        } else {
            XposedBridge.log("Cannot find [METHOD_SEND_MSG]")
        }
    }

    fun sendMessage(record: MessageRecord) {

    }
}