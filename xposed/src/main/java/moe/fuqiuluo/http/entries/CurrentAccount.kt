package moe.fuqiuluo.http.entries

import kotlinx.serialization.Serializable

@Serializable
data class CurrentAccount(
    var uin: Long,
    var isLogin: Boolean,
    var nick: String = ""
)
