package moe.fuqiuluo.xposed.actions

import android.content.Context

internal interface IAction {

    operator fun invoke(ctx: Context)

}