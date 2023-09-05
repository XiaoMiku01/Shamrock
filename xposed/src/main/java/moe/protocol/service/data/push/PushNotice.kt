package moe.protocol.service.data.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class NoticeType {
    @SerialName("group_ban") GroupBan,
    @SerialName("group_admin") GroupAdminChange,
    @SerialName("group_decrease") GroupMemDecrease,
    @SerialName("group_increase") GroupMemIncrease,
    @SerialName("group_recall") GroupRecall,
    @SerialName("friend_recall") FriendRecall,
    @SerialName("notify") Notify,
}

@Serializable
internal enum class NoticeSubType {
    @SerialName("ban") Ban,
    @SerialName("lift_ban") LiftBan,

    @SerialName("set") Set,
    @SerialName("un_set") UnSet,

    @SerialName("invite") Invite,
    @SerialName("approve") Approve,
    @SerialName("leave") Leave,
    @SerialName("kick") Kick,
    @SerialName("kick_me") KickMe,

    @SerialName("poke") Poke,
}

@Serializable
internal data class PushNotice(
    @SerialName("time") val time: Long,
    @SerialName("self_id") val selfId: Long,
    @SerialName("post_type") val postType: String,
    @SerialName("notice_type") val type: NoticeType,
    @SerialName("sub_type") val subType: NoticeSubType,
    @SerialName("group_id") val groupId: Long = 0,
    @SerialName("operator_id") val operatorId: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("duration") val duration: Int,
    @SerialName("message_id") val msgId: Long,
    @SerialName("tip_text") val tip: String,
    @SerialName("target_id") val target: Long,
)
