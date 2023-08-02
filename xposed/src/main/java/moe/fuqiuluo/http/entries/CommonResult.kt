package moe.fuqiuluo.http.entries

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CommonResult<T>(
    var status: String,
    var retcode: Int,
    @Contextual
    var data: T
)
