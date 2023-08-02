package moe.fuqiuluo.http.entries

import kotlinx.serialization.Serializable

@Serializable
data class ErrorCatch(
    var url: String,
    var error: String
)
