package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.troop.api.ITroopInfoService
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.entries.EmptyObject
import mqq.app.MobileQQ

internal object ModifyTroopName: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        if (!session.has("group_id")) {
            return noParam("group_id")
        }
        val groupId = session.getString("group_id")

        if (!session.has("group_name")) {
            return noParam("group_name")
        }
        val groupName = session.getString("group_name")

        val runtime = MobileQQ.getMobileQQ().waitAppRuntime() as QQAppInterface

        val service = runtime
            .getRuntimeService(ITroopInfoService::class.java, "all")
        val groupInfo = service.getTroopInfo(groupId)
        return if (groupInfo.isAdmin || groupInfo.troopowneruin == runtime.account) {
            GroupSvc.modifyTroopName(groupId, groupName)
            ok(EmptyObject)
        } else {
            logic("You are not the administrator of the group")
        }
    }

    override fun path(): String = "set_group_name"
}