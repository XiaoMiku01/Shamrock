package com.tencent.mobileqq.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
    data class VersionInfo (
        val impl: String,
        val version: String,
        @SerialName("onebot_version")
        val onebotVersion: String
    )