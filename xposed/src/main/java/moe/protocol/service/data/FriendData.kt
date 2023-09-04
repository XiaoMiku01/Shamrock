package moe.protocol.service.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FriendEntry(
    @SerialName("user_id") val id: String?,
    @SerialName("user_name") val name: String?,
    @SerialName("user_displayname") val displayName: String?,
    @SerialName("user_remark") val remark: String?,
    val age: Int,
    val gender: Byte,
    @SerialName("group_id") val groupId: Int,
    @SerialName("platform") val platformType: PlatformType,
    @SerialName("term_type") val termType: Int,
)

