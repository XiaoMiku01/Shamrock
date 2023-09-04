package moe.protocol.service.data

import moe.protocol.service.data.push.MemberRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SimpleTroopInfo(
    @SerialName("group_id") val groupId: String,
    @SerialName("group_name") val groupName: String?,
    @SerialName("group_remark") val groupRemark: String?,
    @SerialName("group_uin") val groupUin: String,
    @SerialName("admins") val adminList: List<Long>,
    @SerialName("class_text") val classText: String?,
    @SerialName("is_frozen") val isFrozen: Boolean,
    //@SerialName("troop_level") val troopLevel: String?,
    @SerialName("max_member") val maxMember: Int,
    @SerialName("member_num") val memNum: Int,
    @SerialName("member_count") val memCount: Int,
    @SerialName("max_member_count") val maxNum: Int,
)

@Serializable
internal data class SimpleTroopMemberInfo(
    @SerialName("user_id") val uin: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("user_name") val name: String,
    @SerialName("sex") val sex: String,
    @SerialName("title") val title: String,
    @SerialName("title_expire_time") val titleExpireTime: Int,
    @SerialName("nickname") val nick: String,
    @SerialName("user_displayname") val showName: String?,
    @SerialName("distance") val distance: Int,
    @SerialName("honor") val honor: List<Int>,
    @SerialName("join_time") val joinTime: Long,
    @SerialName("last_active_time") val lastActiveTime: Long,
    @SerialName("last_sent_time") val lastSentTime: Long,
    @SerialName("unique_name") val uniqueName: String?,
    @SerialName("area") val area: String,
    @SerialName("level") val level: Int,
    @SerialName("role") val role: MemberRole,
    @SerialName("unfriendly") val unfriendly: Boolean,
    @SerialName("card_changeable") val cardChangeable: Boolean,
)