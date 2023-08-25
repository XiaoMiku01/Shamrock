package com.tencent.mobileqq.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
internal enum class MsgSubType {
    /**
     * 群聊子类型
     */
    @SerialName("normal") NORMAL,
    @SerialName("anonymous") ANONYMOUS,
    @SerialName("notice") NOTICE,

    /**
     * 私聊子类型
     */
    @SerialName("group") GroupLess,
    @SerialName("friend") Friend,
    @SerialName("other") Other,
}

@Serializable
internal enum class MsgType {
    @SerialName("group") Group,
    @SerialName("private") Private
}

@Serializable
internal data class PushMessage (
    @SerialName("time") val time: Long,
    @SerialName("self_id") val selfId: Long,
    @SerialName("post_type") val postType: String,
    @SerialName("message_type") val messageType: MsgType,
    @SerialName("sub_type") val subType: MsgSubType,
    @SerialName("message_id") val messageId: Int,
    @SerialName("group_id") val groupId: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("anonymous") val anonymous: Anonymous? = null,
    @SerialName("message") val message: JsonElement,
    @SerialName("raw_message") val rawMessage: String,
    @SerialName("font") val font: Int,
    @SerialName("sender") val sender: Sender
)

@Serializable
internal data class Anonymous(
    @SerialName("name") val name: String
)

@Serializable
internal enum class MemberRole {
    @SerialName("owner") Owner,
    @SerialName("admin") Admin,
    @SerialName("member") Member
}

@Serializable
internal data class Sender(
    @SerialName("user_id") val userId: Long,
    @SerialName("nickname") val nickname: String,
    @SerialName("card") val card: String,
    @SerialName("role") val role: MemberRole?,
    @SerialName("title") val title: String,
    @SerialName("level") val level: String,
)