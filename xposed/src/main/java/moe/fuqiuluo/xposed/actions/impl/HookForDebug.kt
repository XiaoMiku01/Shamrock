package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.xposed.actions.IAction

internal class HookForDebug: IAction {
    override fun invoke(ctx: Context) {
        // MessageHelper.hookSendMessageOldChannel()
    }
}