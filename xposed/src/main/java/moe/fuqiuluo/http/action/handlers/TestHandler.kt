package moe.fuqiuluo.http.action.handlers

import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.ActionHandler
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.entries.CommonResult
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.result

internal object TestHandler: ActionHandler<TestHandler.Test> {
    override fun handle(session: ActionSession): CommonResult<Test?> {
        return result(true, Status.Ok, Test(System.currentTimeMillis()))
    }

    @Serializable
    data class Test(val time: Long)
}