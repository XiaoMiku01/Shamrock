package com.tencent.mobileqq.data

import kotlinx.serialization.Serializable

@Serializable
internal data class OutResource(
    val file: String,
    val url: String
)