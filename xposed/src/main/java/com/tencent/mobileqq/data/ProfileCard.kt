package com.tencent.mobileqq.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileCard(
    @SerialName("user_id") val uin: String,
    @SerialName("user_name") val name: String,
    @SerialName("user_displayname") val displayName: String?,
    @SerialName("user_remark") val remark: String?,
    val mail: String?,
    @SerialName("find_method") val findMethod: String?,

    @SerialName("max_vote_cnt") val maxVoteCnt: Short,
    @SerialName("have_vote_cnt") val haveVoteCnt: Short,

    @SerialName("vip_list") val vipList: List<VipInfo>,

    @SerialName("hobby_entry") val hobbyEntry: String?,

    val level: Int,
    val birthday: Long,
    @SerialName("login_day") val loginDay: Long,

    @SerialName("vote_cnt") val voteCnt: Long,

    val qid: String,
    @SerialName("is_school_verified") val schoolVerified: Boolean,

    val location: Location,
)

@Serializable
data class Location(
    val city: String?,
    val company: String?,
    val country: String?,
    val province: String?,
    val hometown: String?,
    val school: String?
)
