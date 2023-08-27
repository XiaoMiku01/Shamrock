package moe.fuqiuluo.remote.action.handlers

import com.tencent.qqnt.protocol.GroupSvc
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler

internal object SetGroupWholeBan: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val groupId = session.getLong("group_id")
        val enable = session.getBoolean("enable")
        return ""
    }

    operator fun invoke(groupId: Long, enable: Boolean): String {
        GroupSvc.setGroupWholeBan(groupId, enable)
        return ok("成功")
    }

    override val requiredParams: Array<String> = arrayOf()

    override fun path(): String = "set_group_whole_ban"
}