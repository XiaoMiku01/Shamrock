package moe.fuqiuluo.http.action.data

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