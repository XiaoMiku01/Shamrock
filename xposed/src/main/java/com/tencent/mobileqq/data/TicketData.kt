package com.tencent.mobileqq.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WebCookies(
    @SerialName("cookies") val cookie: String
)