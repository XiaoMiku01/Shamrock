package com.tencent.mobileqq.data.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class NoticeType {
    @SerialName("group_ban") GroupBan,
    @SerialName("group_admin") GroupAdminChange
}

@Serializable
internal enum class NoticeSubType {
    @SerialName("ban") Ban,
    @SerialName("lift_ban") LiftBan,

    @SerialName("set") Set,
    @SerialName("un_set") UnSet,
}

@Serializable
internal data class PushNotice (
    @SerialName("time") val time: Long,
    @SerialName("self_id") val selfId: Long,
    @SerialName("post_type") val postType: String,
    @SerialName("notice_type") val type: NoticeType,
    @SerialName("sub_type") val subType: NoticeSubType,
    @SerialName("group_id") val groupId: Long = 0,
    @SerialName("operator_id") val operatorId: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("duration") val duration: Int = 0,
)
