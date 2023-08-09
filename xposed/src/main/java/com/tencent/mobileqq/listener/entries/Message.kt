package com.tencent.mobileqq.listener.entries

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MessageType {
    @SerialName("private") PRIVATE,
    @SerialName("group") GROUP
}

@Serializable
data class MessageChain(
    val type: MessageChainType,
    @Contextual
    val data: MessageChainContent
)

@Serializable
data class Message(
    @SerialName("message_id") val messageId: Long,
    @SerialName("user_id") val userId: Long,
    val time: Long,
    @SerialName("message_type") val messageType: MessageType,
    val message: ArrayList<MessageChain>,
    @SerialName("post_type") val postType: String
)
