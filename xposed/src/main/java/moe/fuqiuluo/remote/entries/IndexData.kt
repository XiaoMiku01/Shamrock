package moe.fuqiuluo.remote.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexData(
    var processName: String,
    var startTime: Long,
    @SerialName("http_version") var httpVersion: String
)
