package moe.protocol.service.data

import kotlinx.serialization.Serializable

@Serializable
internal data class OutResource(
    val file: String,
    val url: String
)