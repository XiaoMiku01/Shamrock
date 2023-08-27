package moe.fuqiuluo.remote.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.EmptyObject
import mqq.app.MobileQQ

internal object LeaveTroop: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        GroupSvc.resignTroop(groupId.toLong())

        return ok(EmptyObject, "请求已提交")
    }

    override val requiredParams: Array<String> = arrayOf("group_id")

    override fun path(): String = "leave_group"
}