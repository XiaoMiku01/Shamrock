package com.tencent.mobileqq.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SimpleTroopInfo(
    @SerialName("group_id") val groupId: String,
    @SerialName("group_name") val groupName: String?,
    @SerialName("group_remark") val groupRemark: String?,
    @SerialName("group_uin") val groupUin: String,
    @SerialName("admins") val adminList: List<String>,
    @SerialName("class_text") val classText: String?,
    @SerialName("is_frozen") val isFrozen: Boolean,
    //@SerialName("troop_level") val troopLevel: String?,
    @SerialName("max_member") val maxMember: Int,
    @SerialName("member_num") val memNum: Int,
)

@Serializable
internal data class SimpleTroopMemberInfo(
    @SerialName("user_id") val uin: String,
    @SerialName("user_name") val name: String,
    @SerialName("user_displayname") val showName: String?,
    @SerialName("distance") val distance: Int,
    @SerialName("honor") val honor: List<Int>,
    @SerialName("join_time") val joinTime: Long,
    @SerialName("last_active_time") val lastActiveTime: Long,
    @SerialName("unique_name") val uniqueName: String?,
)