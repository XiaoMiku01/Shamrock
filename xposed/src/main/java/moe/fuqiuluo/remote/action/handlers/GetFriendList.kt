package moe.fuqiuluo.remote.action.handlers

import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import com.tencent.mobileqq.data.FriendEntry
import com.tencent.mobileqq.data.PlatformType
import com.tencent.qqnt.protocol.FriendSvc

internal object GetFriendList: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val refresh = session.getBooleanOrDefault("refresh", false)
        return invoke(refresh)
    }

    suspend operator fun invoke(refresh: Boolean): String {
        val friendList = FriendSvc.getFriendList(refresh)
            ?: return error("get friendlist failed, please check your account or network.")
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
        })
    }


    override fun path(): String = "get_friend_list"
}