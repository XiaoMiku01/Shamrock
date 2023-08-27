package moe.fuqiuluo.remote.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.troop.api.ITroopInfoService
import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.remote.entries.EmptyObject
import mqq.app.MobileQQ

internal object ModifyTroopName: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getString("group_id")
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

    override val requiredParams: Array<String> = arrayOf("group_id", "group_name")

    override fun path(): String = "set_group_name"
}