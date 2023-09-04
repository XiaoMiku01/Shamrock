package moe.protocol.service.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val HONOR_TALKATIVE = 1
internal const val HONOR_GROUP_FIRE = 2
internal const val HONOR_GROUP_FLAME = 4
internal const val HONOR_NEWBIE = 5
internal const val HONOR_HAPPY = 6
internal const val HONOR_ACADEMIC_STAR = 7
internal const val HONOR_TOP_STUDENT = 8
internal const val HONOR_TOP_GOD = 9
internal const val HONOR_LEADING = 10
internal const val HONOR_NEWBIE_2 = 11
internal const val HONOR_ATMOSPHERE = 12
internal const val HONOR_GIFT = 13

@Serializable
internal data class GroupMemberHonor(
    @SerialName("use_id") val userId: String,
    @SerialName("nickname") var nick: String,
    @SerialName("avatar") val avatar: String,
    @SerialName("day_count") val dayCount: Int,
    @SerialName("id") val id: Int,
    @SerialName("description") val desc: String,

)

@Serializable
internal data class GroupAllHonor(
    @SerialName("group_id") val groupId: String,
    @SerialName("current_talkative") val currentTalkActive: GroupMemberHonor?,
    @SerialName("talkative_list") val talkativeList: List<GroupMemberHonor>?,
    @SerialName("performer_list") val performerList: List<GroupMemberHonor>?,
    @SerialName("legend_list") val legendList: List<GroupMemberHonor>?,
    @SerialName("strong_newbie_list") val strongNewbieList: List<GroupMemberHonor>?,
    @SerialName("emotion_list") val emotionList: List<GroupMemberHonor>?,
    @SerialName("all") val all: List<GroupMemberHonor>?,
)