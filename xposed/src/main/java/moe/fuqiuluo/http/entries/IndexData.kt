package moe.fuqiuluo.http.entries

import kotlinx.serialization.Serializable

@Serializable
data class IndexData(
    var processName: String,
    var startTime: Long
)
