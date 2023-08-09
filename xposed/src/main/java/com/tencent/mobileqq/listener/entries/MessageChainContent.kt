package com.tencent.mobileqq.listener.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MessageChainType{
    @SerialName("at") AT,
    @SerialName("text") TEXT,
    @SerialName("image") IMAGE
}

sealed class MessageChainContent {
    @Serializable
    data class AT(
        val qq: String
    ): MessageChainContent()

    @Serializable
    data class Text(
        val text: String
    ): MessageChainContent()

    @Serializable
    data class Image(
        val file: String,
        val url: String,
        val subType: String
    ): MessageChainContent()
}