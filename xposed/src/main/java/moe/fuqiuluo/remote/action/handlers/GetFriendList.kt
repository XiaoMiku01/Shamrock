package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.protocol.service.data.FriendEntry
import moe.protocol.service.data.PlatformType
import moe.protocol.servlet.protocol.FriendSvc

internal object GetFriendList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(refresh, session.echo)
    }

    suspend operator fun invoke(refresh: Boolean, echo: String = ""): String {
        val friendList = FriendSvc.getFriendList(refresh)
            ?: return error("get friendlist failed, please check your account or network.", echo)
        return ok(friendList.map { friend ->
            FriendEntry(
                id = friend.uin,
                name = friend.name,
                displayName = friend.remark,
                remark = friend.remark,
                age = friend.age,
                gender = friend.gender,
                groupId = friend.groupid,
                platformType = PlatformType.valueOf(friend.iTermType),
                termType = friend.iTermType
            )
        }, echo)
    }


    override fun path(): String = "get_friend_list"
}