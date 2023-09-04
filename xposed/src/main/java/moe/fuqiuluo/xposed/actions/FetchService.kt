@file:OptIn(DelicateCoroutinesApi::class)
package moe.fuqiuluo.xposed.actions

import android.content.Context
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.nativeinterface.IQQNTWrapperSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.helper.NTServiceFetcher
import moe.fuqiuluo.xposed.tools.hookMethod
import mqq.app.AppRuntime
import mqq.app.api.IRuntimeService

internal class FetchService: IAction {
    override fun invoke(ctx: Context) {
        IQQNTWrapperSession.CppProxy::class.java.hookMethod("startNT").after {
            NTServiceFetcher.onNTStart()
        }
        AppRuntime::class.java.hookMethod("getRuntimeService").after {
            val service = it.result as? IRuntimeService
            if (service != null && service is IKernelService) {
                GlobalScope.launch {
                    NTServiceFetcher.onFetch(service)
                }
            }
        }
    }
}